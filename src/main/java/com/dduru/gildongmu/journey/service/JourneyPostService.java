package com.dduru.gildongmu.journey.service;

import com.dduru.gildongmu.common.time.TimeProvider;
import com.dduru.gildongmu.common.validation.S3ImageUrlValidator;
import com.dduru.gildongmu.journey.domain.Journey;
import com.dduru.gildongmu.journey.domain.JourneyPost;
import com.dduru.gildongmu.journey.domain.enums.JourneyMemberStatus;
import com.dduru.gildongmu.journey.dto.request.JourneyPostCreateRequest;
import com.dduru.gildongmu.journey.dto.request.JourneyPostListRequest;
import com.dduru.gildongmu.journey.dto.request.JourneyPostUpdateRequest;
import com.dduru.gildongmu.journey.dto.response.JourneyPostListResponse;
import com.dduru.gildongmu.journey.dto.response.JourneyPostResponse;
import com.dduru.gildongmu.journey.exception.InvalidJourneyPostException;
import com.dduru.gildongmu.journey.exception.JourneyAccessDeniedException;
import com.dduru.gildongmu.journey.exception.JourneyHostNotFoundException;
import com.dduru.gildongmu.journey.exception.JourneyPostAccessDeniedException;
import com.dduru.gildongmu.journey.repository.JourneyMemberRepository;
import com.dduru.gildongmu.journey.repository.JourneyPostRepository;
import com.dduru.gildongmu.journey.repository.JourneyRepository;
import com.dduru.gildongmu.profile.utils.ProfileImageResolver;
import com.dduru.gildongmu.s3.enums.S3ImageDirectory;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class JourneyPostService {
    private static final int CONTENT_MAX_LENGTH = 300;

    private final JourneyRepository journeyRepository;
    private final JourneyMemberRepository journeyMemberRepository;
    private final JourneyPostRepository journeyPostRepository;
    private final UserRepository userRepository;
    private final S3ImageUrlValidator s3ImageUrlValidator;
    private final ProfileImageResolver profileImageResolver;
    private final TimeProvider timeProvider;

    @Transactional(readOnly = true)
    public JourneyPostListResponse retrievePosts(Long journeyId, Long userId, JourneyPostListRequest request) {
        JourneyPostListRequest normalizedRequest = normalizeRequest(request);
        Journey journey = getAccessibleJourney(journeyId, userId);
        Long hostUserId = findActiveHostUserId(journeyId);
        JourneyPost cursorPost = findCursorPost(journeyId, normalizedRequest.cursor());
        int size = normalizedRequest.sizeOrDefault();

        List<JourneyPost> fetchedPosts = journeyPostRepository.findActivePostsByJourneyIdWithAuthorProfile(
                journeyId,
                cursorPost == null ? null : cursorPost.isNotice(),
                cursorPost == null ? null : cursorPost.getCreatedAt(),
                cursorPost == null ? null : cursorPost.getId(),
                PageRequest.of(0, size + 1)
        );
        boolean hasNext = fetchedPosts.size() > size;
        List<JourneyPost> journeyPosts = trimLookAheadPosts(fetchedPosts, size);

        return JourneyPostListResponse.of(
                journey.getId(),
                journeyPosts,
                hasNext,
                userId,
                hostUserId,
                profileImageResolver
        );
    }

    @Transactional(readOnly = true)
    public JourneyPostResponse retrievePost(Long journeyId, Long journeyPostId, Long userId) {
        getAccessibleJourney(journeyId, userId);
        Long hostUserId = findActiveHostUserId(journeyId);

        JourneyPost journeyPost = journeyPostRepository.getActivePostByIdAndJourneyIdOrThrow(journeyPostId, journeyId);
        return JourneyPostResponse.from(journeyPost, userId, hostUserId, profileImageResolver);
    }

    public JourneyPostResponse createPost(Long journeyId, Long userId, JourneyPostCreateRequest request) {
        Journey journey = getAccessibleJourney(journeyId, userId);
        Long hostUserId = findActiveHostUserId(journeyId);
        User author = userRepository.getByIdOrThrow(userId);

        JourneyPost journeyPost = createJourneyPost(journey, author, request);
        JourneyPost savedPost = journeyPostRepository.saveAndFlush(journeyPost);

        log.info("나의 여정 게시글 생성됨 - journeyId={}, journeyPostId={}, userId={}",
                journeyId, savedPost.getId(), userId);
        return JourneyPostResponse.from(savedPost, userId, hostUserId, profileImageResolver);
    }

    public JourneyPostResponse updatePost(
            Long journeyId,
            Long journeyPostId,
            Long userId,
            JourneyPostUpdateRequest request
    ) {
        JourneyPost journeyPost = getOwnedJourneyPost(journeyId, journeyPostId, userId);
        Long hostUserId = findActiveHostUserId(journeyId);

        String content = normalizeContentPatch(request.content());
        boolean applyImageUrlPatch = request.imageUrl() != null;
        String imageUrl = applyImageUrlPatch ? normalizeImageUrl(request.imageUrl()) : null;
        validateHasAnyPatch(content, applyImageUrlPatch);

        updateJourneyPost(journeyPost, content, applyImageUrlPatch, imageUrl);
        journeyPostRepository.flush();

        log.info("나의 여정 게시글 수정됨 - journeyId={}, journeyPostId={}, userId={}",
                journeyId, journeyPostId, userId);
        return JourneyPostResponse.from(journeyPost, userId, hostUserId, profileImageResolver);
    }

    public void deletePost(Long journeyId, Long journeyPostId, Long userId) {
        JourneyPost journeyPost = getOwnedJourneyPost(journeyId, journeyPostId, userId);

        journeyPost.delete(userId, timeProvider.now());
        log.info("나의 여정 게시글 삭제됨 - journeyId={}, journeyPostId={}, userId={}",
                journeyId, journeyPostId, userId);
    }

    private JourneyPost createJourneyPost(Journey journey, User author, JourneyPostCreateRequest request) {
        return JourneyPost.create(
                journey,
                author,
                normalizeContent(request.content()),
                normalizeImageUrl(request.imageUrl())
        );
    }

    private void updateJourneyPost(
            JourneyPost journeyPost,
            String content,
            boolean applyImageUrlPatch,
            String imageUrl
    ) {
        journeyPost.update(content, applyImageUrlPatch, imageUrl);
    }

    private static JourneyPostListRequest normalizeRequest(JourneyPostListRequest request) {
        if (request == null) {
            return new JourneyPostListRequest(null, null);
        }
        return request;
    }

    private JourneyPost findCursorPost(Long journeyId, Long cursor) {
        if (cursor == null) {
            return null;
        }
        return journeyPostRepository.getActivePostByIdAndJourneyIdOrThrow(cursor, journeyId);
    }

    private static List<JourneyPost> trimLookAheadPosts(List<JourneyPost> posts, int size) {
        if (posts.size() <= size) {
            return posts;
        }
        return new ArrayList<>(posts.subList(0, size));
    }

    private Journey getAccessibleJourney(Long journeyId, Long userId) {
        Journey journey = journeyRepository.getByIdOrThrow(journeyId);
        boolean isActiveMember = journeyMemberRepository.existsByJourneyIdAndUserIdAndStatus(
                journeyId,
                userId,
                JourneyMemberStatus.ACTIVE
        );
        if (!isActiveMember) {
            throw new JourneyAccessDeniedException();
        }
        return journey;
    }

    private JourneyPost getOwnedJourneyPost(Long journeyId, Long journeyPostId, Long userId) {
        getAccessibleJourney(journeyId, userId);

        JourneyPost journeyPost = journeyPostRepository.getActivePostByIdAndJourneyIdOrThrow(journeyPostId, journeyId);
        if (!journeyPost.isAuthor(userId)) {
            throw new JourneyPostAccessDeniedException();
        }
        return journeyPost;
    }

    private Long findActiveHostUserId(Long journeyId) {
        return journeyMemberRepository.findActiveHostUserIdByJourneyId(journeyId)
                .orElseThrow(JourneyHostNotFoundException::new);
    }

    private void validateHasAnyPatch(String content, boolean applyImageUrlPatch) {
        if (content == null && !applyImageUrlPatch) {
            throw InvalidJourneyPostException.emptyPatch();
        }
    }

    private String normalizeContent(String content) {
        if (!StringUtils.hasText(content)) {
            throw InvalidJourneyPostException.invalidContent();
        }

        String normalizedContent = content.trim();
        int length = normalizedContent.codePointCount(0, normalizedContent.length());
        if (length > CONTENT_MAX_LENGTH) {
            throw InvalidJourneyPostException.invalidContent();
        }
        return normalizedContent;
    }

    private String normalizeContentPatch(String content) {
        if (content == null) {
            return null;
        }
        return normalizeContent(content);
    }

    private String normalizeImageUrl(String imageUrl) {
        if (!StringUtils.hasText(imageUrl)) {
            return null;
        }
        return s3ImageUrlValidator.validateAndNormalize(imageUrl, S3ImageDirectory.JOURNEY_POSTS);
    }
}
