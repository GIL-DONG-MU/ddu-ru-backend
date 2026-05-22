package com.dduru.gildongmu.journey.service;

import com.dduru.gildongmu.common.time.TimeProvider;
import com.dduru.gildongmu.journey.domain.JourneyPost;
import com.dduru.gildongmu.journey.domain.JourneyPostComment;
import com.dduru.gildongmu.journey.domain.enums.JourneyMemberStatus;
import com.dduru.gildongmu.journey.dto.request.JourneyPostCommentCreateRequest;
import com.dduru.gildongmu.journey.dto.request.JourneyPostCommentUpdateRequest;
import com.dduru.gildongmu.journey.dto.response.JourneyPostCommentListResponse;
import com.dduru.gildongmu.journey.dto.response.JourneyPostCommentResponse;
import com.dduru.gildongmu.journey.exception.InvalidJourneyPostCommentException;
import com.dduru.gildongmu.journey.exception.JourneyAccessDeniedException;
import com.dduru.gildongmu.journey.exception.JourneyPostCommentAccessDeniedException;
import com.dduru.gildongmu.journey.repository.JourneyMemberRepository;
import com.dduru.gildongmu.journey.repository.JourneyPostCommentRepository;
import com.dduru.gildongmu.journey.repository.JourneyPostRepository;
import com.dduru.gildongmu.journey.repository.JourneyRepository;
import com.dduru.gildongmu.profile.utils.ProfileImageResolver;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class JourneyPostCommentService {
    private static final int CONTENT_MAX_LENGTH = 300;
    private static final int DEFAULT_COMMENT_LIMIT = 20;
    private static final int MAX_COMMENT_LIMIT = 50;

    private final JourneyRepository journeyRepository;
    private final JourneyMemberRepository journeyMemberRepository;
    private final JourneyPostRepository journeyPostRepository;
    private final JourneyPostCommentRepository journeyPostCommentRepository;
    private final UserRepository userRepository;
    private final ProfileImageResolver profileImageResolver;
    private final TimeProvider timeProvider;

    @Transactional(readOnly = true)
    public JourneyPostCommentListResponse retrieveComments(
            Long journeyId,
            Long journeyPostId,
            Long userId,
            Integer limit
    ) {
        JourneyPost journeyPost = getAccessibleJourneyPost(journeyId, journeyPostId, userId);
        Long hostUserId = findActiveHostUserId(journeyId);
        int commentLimit = normalizeCommentLimit(limit);

        List<JourneyPostComment> fetchedComments = journeyPostCommentRepository
                .findActiveCommentsByJourneyPostIdWithAuthorProfile(
                        journeyPostId,
                        PageRequest.of(0, commentLimit + 1)
                );
        boolean hasMore = fetchedComments.size() > commentLimit;
        List<JourneyPostComment> comments = hasMore
                ? List.copyOf(fetchedComments.subList(0, commentLimit))
                : fetchedComments;
        long commentCount = journeyPostCommentRepository.countByJourneyPost_IdAndIsDeletedFalse(journeyPostId);

        return JourneyPostCommentListResponse.of(
                journeyPost.getId(),
                commentCount,
                hasMore,
                comments,
                userId,
                hostUserId,
                profileImageResolver
        );
    }

    public JourneyPostCommentResponse createComment(
            Long journeyId,
            Long journeyPostId,
            Long userId,
            JourneyPostCommentCreateRequest request
    ) {
        JourneyPost journeyPost = getAccessibleJourneyPost(journeyId, journeyPostId, userId);
        Long hostUserId = findActiveHostUserId(journeyId);
        User author = userRepository.getByIdOrThrow(userId);

        JourneyPostComment comment = createJourneyPostComment(journeyPost, author, request);
        JourneyPostComment savedComment = journeyPostCommentRepository.saveAndFlush(comment);

        log.info("나의 여정 게시글 댓글 생성됨 - journeyId={}, journeyPostId={}, commentId={}, userId={}",
                journeyId, journeyPostId, savedComment.getId(), userId);
        return JourneyPostCommentResponse.from(savedComment, journeyPostId, userId, hostUserId, profileImageResolver);
    }

    public JourneyPostCommentResponse updateComment(
            Long journeyId,
            Long journeyPostId,
            Long commentId,
            Long userId,
            JourneyPostCommentUpdateRequest request
    ) {
        getAccessibleJourneyPost(journeyId, journeyPostId, userId);
        Long hostUserId = findActiveHostUserId(journeyId);
        JourneyPostComment comment = getOwnedComment(journeyPostId, commentId, userId);

        String content = normalizeContentPatch(request.content());
        validateHasAnyPatch(content);

        updateJourneyPostComment(comment, content);
        journeyPostCommentRepository.flush();

        log.info("나의 여정 게시글 댓글 수정됨 - journeyId={}, journeyPostId={}, commentId={}, userId={}",
                journeyId, journeyPostId, commentId, userId);
        return JourneyPostCommentResponse.from(comment, journeyPostId, userId, hostUserId, profileImageResolver);
    }

    public void deleteComment(Long journeyId, Long journeyPostId, Long commentId, Long userId) {
        getAccessibleJourneyPost(journeyId, journeyPostId, userId);
        JourneyPostComment comment = getOwnedComment(journeyPostId, commentId, userId);

        comment.softDelete(userId, timeProvider.now());
        log.info("나의 여정 게시글 댓글 삭제됨 - journeyId={}, journeyPostId={}, commentId={}, userId={}",
                journeyId, journeyPostId, commentId, userId);
    }

    private JourneyPostComment createJourneyPostComment(
            JourneyPost journeyPost,
            User author,
            JourneyPostCommentCreateRequest request
    ) {
        return JourneyPostComment.create(
                journeyPost,
                author,
                normalizeContent(request.content())
        );
    }

    private void updateJourneyPostComment(JourneyPostComment comment, String content) {
        comment.updateContent(content);
    }

    private JourneyPost getAccessibleJourneyPost(Long journeyId, Long journeyPostId, Long userId) {
        validateActiveMember(journeyId, userId);
        return journeyPostRepository.getActivePostByIdAndJourneyIdOrThrow(journeyPostId, journeyId);
    }

    private void validateActiveMember(Long journeyId, Long userId) {
        journeyRepository.getByIdOrThrow(journeyId);
        boolean isActiveMember = journeyMemberRepository.existsByJourneyIdAndUserIdAndStatus(
                journeyId,
                userId,
                JourneyMemberStatus.ACTIVE
        );
        if (!isActiveMember) {
            throw new JourneyAccessDeniedException();
        }
    }

    private JourneyPostComment getOwnedComment(Long journeyPostId, Long commentId, Long userId) {
        JourneyPostComment comment = journeyPostCommentRepository
                .getActiveCommentByIdAndJourneyPostIdOrThrow(commentId, journeyPostId);
        if (!comment.isAuthor(userId)) {
            throw new JourneyPostCommentAccessDeniedException();
        }
        return comment;
    }

    private Long findActiveHostUserId(Long journeyId) {
        return journeyMemberRepository.findActiveHostUserIdByJourneyId(journeyId)
                .orElse(null);
    }

    private int normalizeCommentLimit(Integer limit) {
        if (limit == null) {
            return DEFAULT_COMMENT_LIMIT;
        }
        if (limit < 1) {
            return 1;
        }
        return Math.min(limit, MAX_COMMENT_LIMIT);
    }

    private void validateHasAnyPatch(String content) {
        if (content == null) {
            throw InvalidJourneyPostCommentException.emptyPatch();
        }
    }

    private String normalizeContent(String content) {
        if (!StringUtils.hasText(content)) {
            throw InvalidJourneyPostCommentException.invalidContent();
        }

        String normalizedContent = content.trim();
        int length = normalizedContent.codePointCount(0, normalizedContent.length());
        if (length > CONTENT_MAX_LENGTH) {
            throw InvalidJourneyPostCommentException.invalidContent();
        }
        return normalizedContent;
    }

    private String normalizeContentPatch(String content) {
        if (content == null) {
            return null;
        }
        return normalizeContent(content);
    }
}
