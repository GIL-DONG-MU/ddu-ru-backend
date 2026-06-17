package com.dduru.gildongmu.journey.service;

import com.dduru.gildongmu.common.time.TimeProvider;
import com.dduru.gildongmu.common.validation.S3ImageUrlValidator;
import com.dduru.gildongmu.journey.domain.Journey;
import com.dduru.gildongmu.journey.domain.JourneyPost;
import com.dduru.gildongmu.journey.domain.JourneyPostImage;
import com.dduru.gildongmu.journey.domain.enums.JourneyMemberStatus;
import com.dduru.gildongmu.journey.dto.request.JourneyPostCreateRequest;
import com.dduru.gildongmu.journey.dto.request.JourneyPostListRequest;
import com.dduru.gildongmu.journey.dto.request.JourneyPostNoticeUpdateRequest;
import com.dduru.gildongmu.journey.dto.request.JourneyPostUpdateRequest;
import com.dduru.gildongmu.journey.dto.response.JourneyPostListResponse;
import com.dduru.gildongmu.journey.dto.response.JourneyPostNoticeUpdateResponse;
import com.dduru.gildongmu.journey.dto.response.JourneyPostResponse;
import com.dduru.gildongmu.journey.exception.InvalidJourneyPostException;
import com.dduru.gildongmu.journey.exception.JourneyAccessDeniedException;
import com.dduru.gildongmu.journey.exception.JourneyHostNotFoundException;
import com.dduru.gildongmu.journey.exception.JourneyPostAccessDeniedException;
import com.dduru.gildongmu.journey.exception.JourneyPostNoticeLimitExceededException;
import com.dduru.gildongmu.journey.repository.JourneyMemberRepository;
import com.dduru.gildongmu.journey.repository.JourneyPostCommentRepository;
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class JourneyPostService {
    private static final int NOTICE_LIMIT = 5;

    private final JourneyRepository journeyRepository;
    private final JourneyMemberRepository journeyMemberRepository;
    private final JourneyPostRepository journeyPostRepository;
    private final JourneyPostCommentRepository journeyPostCommentRepository;
    private final UserRepository userRepository;
    private final S3ImageUrlValidator s3ImageUrlValidator;
    private final ProfileImageResolver profileImageResolver;
    private final TimeProvider timeProvider;

    @Transactional(readOnly = true)
    public JourneyPostListResponse retrievePosts(Long journeyId, Long userId, JourneyPostListRequest request) {
        JourneyPostListRequest normalizedRequest = normalizeRequest(request);
        validateJourneyAccess(journeyId, userId);
        Long hostUserId = findActiveHostUserId(journeyId);
        JourneyPost cursorPost = findCursorPost(journeyId, normalizedRequest.cursor());
        int size = normalizedRequest.size();

        List<JourneyPost> fetchedPosts = journeyPostRepository.findActivePostsByJourneyIdWithAuthorProfile(
                journeyId,
                cursorPost == null ? null : cursorPost.isNotice(),
                cursorPost == null ? null : cursorPost.getCreatedAt(),
                cursorPost == null ? null : cursorPost.getId(),
                PageRequest.of(0, size + 1)
        );
        boolean hasNext = fetchedPosts.size() > size;
        List<JourneyPost> journeyPosts = trimLookAheadPosts(fetchedPosts, size);

        List<Long> postIds = journeyPosts.stream().map(JourneyPost::getId).toList();
        Map<Long, Long> commentCountByPostId = journeyPostCommentRepository.getCommentCountsByJourneyPostIds(postIds);
        LocalDate today = today();

        List<JourneyPostResponse> posts = journeyPosts.stream()
                .map(post -> JourneyPostResponse.from(
                        post, userId, hostUserId, profileImageResolver,
                        commentCountByPostId.getOrDefault(post.getId(), 0L),
                        today
                ))
                .toList();

        return JourneyPostListResponse.of(journeyId, posts, hasNext);
    }

    @Transactional(readOnly = true)
    public JourneyPostResponse retrievePost(Long journeyId, Long journeyPostId, Long userId) {
        validateJourneyAccess(journeyId, userId);
        Long hostUserId = findActiveHostUserId(journeyId);

        JourneyPost journeyPost = journeyPostRepository.getActivePostByIdAndJourneyIdOrThrow(journeyPostId, journeyId);
        long commentCount = journeyPostCommentRepository.countByJourneyPost_IdAndIsDeletedFalse(journeyPostId);
        return JourneyPostResponse.from(journeyPost, userId, hostUserId, profileImageResolver, commentCount, today());
    }

    public JourneyPostResponse createPost(Long journeyId, Long userId, JourneyPostCreateRequest request) {
        Journey journey = getAccessibleJourney(journeyId, userId);
        Long hostUserId = findActiveHostUserId(journeyId);
        User author = userRepository.getByIdOrThrow(userId);

        JourneyPost journeyPost = JourneyPost.create(journey, author, request.content());
        JourneyPost savedPost = journeyPostRepository.saveAndFlush(journeyPost);
        savedPost.replaceImages(createImages(savedPost, request.imageUrls()));

        log.info("나의 여정 게시글 생성됨 - journeyId={}, journeyPostId={}, userId={}",
                journeyId, savedPost.getId(), userId);
        return JourneyPostResponse.from(savedPost, userId, hostUserId, profileImageResolver, 0L, today());
    }

    public JourneyPostResponse updatePost(
            Long journeyId,
            Long journeyPostId,
            Long userId,
            JourneyPostUpdateRequest request
    ) {
        JourneyPost journeyPost = getOwnedJourneyPost(journeyId, journeyPostId, userId);
        Long hostUserId = findActiveHostUserId(journeyId);

        String content = request.content();
        boolean applyImagesPatch = request.imageUrls() != null;
        List<JourneyPostImage> newImages = applyImagesPatch ? createImages(journeyPost, request.imageUrls()) : Collections.emptyList();
        validateHasAnyPatch(content, applyImagesPatch);

        journeyPost.update(content, applyImagesPatch, newImages);
        journeyPostRepository.flush();

        log.info("나의 여정 게시글 수정됨 - journeyId={}, journeyPostId={}, userId={}",
                journeyId, journeyPostId, userId);
        long commentCount = journeyPostCommentRepository.countByJourneyPost_IdAndIsDeletedFalse(journeyPostId);
        return JourneyPostResponse.from(journeyPost, userId, hostUserId, profileImageResolver, commentCount, today());
    }

    public void deletePost(Long journeyId, Long journeyPostId, Long userId) {
        validateJourneyAccess(journeyId, userId);
        JourneyPost journeyPost = journeyPostRepository.getActivePostByIdAndJourneyIdOrThrow(journeyPostId, journeyId);

        if (!journeyPost.isAuthor(userId)) {
            validateActiveHost(journeyId, userId);
        }

        journeyPost.delete(userId, timeProvider.now());
        log.info("나의 여정 게시글 삭제됨 - journeyId={}, journeyPostId={}, userId={}",
                journeyId, journeyPostId, userId);
    }

    public JourneyPostNoticeUpdateResponse updatePostNotice(
            Long journeyId,
            Long journeyPostId,
            Long userId,
            JourneyPostNoticeUpdateRequest request
    ) {
        validateActiveHost(journeyId, userId);

        JourneyPost journeyPost = journeyPostRepository.getActivePostByIdAndJourneyIdOrThrow(journeyPostId, journeyId);
        boolean nextNotice = Boolean.TRUE.equals(request.isNotice());
        validateNoticeLimitBeforeMarking(journeyId, userId, journeyPost, nextNotice);

        journeyPost.updateNoticeStatus(nextNotice);
        log.info("나의 여정 게시글 공지 상태 변경됨 - journeyId={}, journeyPostId={}, isNotice={}, userId={}",
                journeyId, journeyPostId, nextNotice, userId);
        return JourneyPostNoticeUpdateResponse.from(journeyPost);
    }

    private List<JourneyPostImage> createImages(JourneyPost post, List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return Collections.emptyList();
        }
        List<JourneyPostImage> images = new ArrayList<>();
        for (int i = 0; i < imageUrls.size(); i++) {
            String normalized = normalizeImageUrl(imageUrls.get(i));
            if (normalized != null) {
                images.add(JourneyPostImage.of(post, normalized, i));
            }
        }
        return images;
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

    private void validateJourneyAccess(Long journeyId, Long userId) {
        if (!journeyMemberRepository.existsByJourneyIdAndUserIdAndStatus(
                journeyId, userId, JourneyMemberStatus.ACTIVE)) {
            throw new JourneyAccessDeniedException();
        }
    }

    private Journey getAccessibleJourney(Long journeyId, Long userId) {
        validateJourneyAccess(journeyId, userId);
        return journeyRepository.getByIdOrThrow(journeyId);
    }

    private JourneyPost getOwnedJourneyPost(Long journeyId, Long journeyPostId, Long userId) {
        validateJourneyAccess(journeyId, userId);

        JourneyPost journeyPost = journeyPostRepository.getActivePostByIdAndJourneyIdOrThrow(journeyPostId, journeyId);
        if (!journeyPost.isAuthor(userId)) {
            throw new JourneyPostAccessDeniedException();
        }
        return journeyPost;
    }

    private void validateActiveHost(Long journeyId, Long userId) {
        boolean isActiveHost = journeyMemberRepository.existsActiveHost(journeyId, userId);
        if (!isActiveHost) {
            throw new JourneyAccessDeniedException();
        }
    }

    private void validateNoticeLimitBeforeMarking(
            Long journeyId,
            Long userId,
            JourneyPost journeyPost,
            boolean nextNotice
    ) {
        if (!nextNotice || journeyPost.isNotice()) {
            return;
        }

        // 새 공지를 추가하는 경로만 직렬화해 여정당 공지 최대 3개 정책을 보장한다.
        journeyRepository.getByIdWithLockOrThrow(journeyId);
        validateActiveHost(journeyId, userId);

        long noticeCount = journeyPostRepository.countActiveNoticesByJourneyId(journeyId);
        if (noticeCount >= NOTICE_LIMIT) {
            throw new JourneyPostNoticeLimitExceededException();
        }
    }

    private Long findActiveHostUserId(Long journeyId) {
        return journeyMemberRepository.findActiveHostUserIdByJourneyId(journeyId)
                .orElseThrow(JourneyHostNotFoundException::new);
    }

    private void validateHasAnyPatch(String content, boolean applyImagesPatch) {
        if (content == null && !applyImagesPatch) {
            throw InvalidJourneyPostException.emptyPatch();
        }
    }

    private String normalizeImageUrl(String imageUrl) {
        if (!StringUtils.hasText(imageUrl)) {
            return null;
        }
        return s3ImageUrlValidator.validateAndNormalize(imageUrl, S3ImageDirectory.JOURNEY_POSTS);
    }

    private LocalDate today() {
        return timeProvider.today();
    }
}
