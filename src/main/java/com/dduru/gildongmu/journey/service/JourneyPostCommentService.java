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
import com.dduru.gildongmu.journey.exception.JourneyHostNotFoundException;
import com.dduru.gildongmu.journey.exception.JourneyPostCommentAccessDeniedException;
import com.dduru.gildongmu.journey.repository.JourneyMemberRepository;
import com.dduru.gildongmu.journey.repository.JourneyPostCommentRepository;
import com.dduru.gildongmu.journey.repository.JourneyPostRepository;
import com.dduru.gildongmu.profile.utils.ProfileImageResolver;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class JourneyPostCommentService {
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
            Long userId
    ) {
        validateJourneyPostAccess(journeyId, journeyPostId, userId);
        Long hostUserId = findActiveHostUserId(journeyId);

        List<JourneyPostComment> comments = journeyPostCommentRepository
                .findActiveCommentsByJourneyPostIdWithAuthorProfile(journeyPostId);

        LocalDate today = today();
        List<JourneyPostCommentResponse> commentResponses = comments.stream()
                .map(comment -> JourneyPostCommentResponse.from(
                        comment, journeyPostId, userId, hostUserId, profileImageResolver, today
                ))
                .toList();

        return JourneyPostCommentListResponse.of(journeyPostId, commentResponses);
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
        return JourneyPostCommentResponse.from(savedComment, journeyPostId, userId, hostUserId, profileImageResolver, today());
    }

    public JourneyPostCommentResponse updateComment(
            Long journeyId,
            Long journeyPostId,
            Long commentId,
            Long userId,
            JourneyPostCommentUpdateRequest request
    ) {
        validateJourneyPostAccess(journeyId, journeyPostId, userId);
        Long hostUserId = findActiveHostUserId(journeyId);
        JourneyPostComment comment = getOwnedComment(journeyPostId, commentId, userId);

        String content = request.content();
        validateHasAnyPatch(content);

        comment.updateContent(content);
        journeyPostCommentRepository.flush();

        log.info("나의 여정 게시글 댓글 수정됨 - journeyId={}, journeyPostId={}, commentId={}, userId={}",
                journeyId, journeyPostId, commentId, userId);
        return JourneyPostCommentResponse.from(comment, journeyPostId, userId, hostUserId, profileImageResolver, today());
    }

    public void deleteComment(Long journeyId, Long journeyPostId, Long commentId, Long userId) {
        validateJourneyPostAccess(journeyId, journeyPostId, userId);
        JourneyPostComment comment = journeyPostCommentRepository
                .getActiveCommentByIdAndJourneyPostIdOrThrow(commentId, journeyPostId);

        if (!comment.isAuthor(userId) && !journeyMemberRepository.existsActiveHost(journeyId, userId)) {
            throw new JourneyPostCommentAccessDeniedException();
        }

        comment.delete(userId, timeProvider.now());
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
                request.content()
        );
    }

    private JourneyPost getAccessibleJourneyPost(Long journeyId, Long journeyPostId, Long userId) {
        if (!journeyMemberRepository.existsByJourneyIdAndUserIdAndStatus(journeyId, userId, JourneyMemberStatus.ACTIVE)) {
            throw new JourneyAccessDeniedException();
        }
        return journeyPostRepository.getActivePostByIdAndJourneyIdOrThrow(journeyPostId, journeyId);
    }

    private void validateJourneyPostAccess(Long journeyId, Long journeyPostId, Long userId) {
        getAccessibleJourneyPost(journeyId, journeyPostId, userId);
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
                .orElseThrow(JourneyHostNotFoundException::new);
    }

    private void validateHasAnyPatch(String content) {
        if (content == null) {
            throw InvalidJourneyPostCommentException.emptyPatch();
        }
    }

    private LocalDate today() {
        return timeProvider.today();
    }
}
