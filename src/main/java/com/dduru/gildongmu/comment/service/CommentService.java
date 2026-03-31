package com.dduru.gildongmu.comment.service;

import com.dduru.gildongmu.comment.domain.Comment;
import com.dduru.gildongmu.comment.dto.request.CommentCreateRequest;
import com.dduru.gildongmu.comment.dto.response.CommentResponse;
import com.dduru.gildongmu.comment.dto.request.CommentUpdateRequest;
import com.dduru.gildongmu.comment.exception.CommentAccessDeniedException;
import com.dduru.gildongmu.comment.exception.CommentNotFoundException;
import com.dduru.gildongmu.comment.exception.InvalidParentCommentException;
import com.dduru.gildongmu.comment.repository.CommentRepository;
import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.post.repository.PostRepository;
import com.dduru.gildongmu.profile.service.ProfileImageResolver;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final ProfileImageResolver profileImageResolver;

    @Transactional
    public CommentResponse create(Long userId, Long postId, CommentCreateRequest request) {
        User user = userRepository.getByIdOrThrow(userId);
        Post post = postRepository.getActiveByIdOrThrow(postId);
        Comment parent = getValidParentComment(request.parentId(), postId);

        Comment comment = Comment.createComment(request.content(), user, post, parent);
        Comment savedComment = commentRepository.save(comment);

        log.info("댓글 생성 완료 - commentId: {}, userId: {}, postId: {}", savedComment.getId(), userId, postId);
        return CommentResponse.from(savedComment, profileImageResolver);
    }

    @Transactional
    public void delete(Long userId, Long postId, Long commentId) {
        Comment comment = commentRepository.findByIdAndDeletedFalse(commentId)
                .orElseThrow(CommentNotFoundException::new);

        validateCommentBelongsToPost(comment, postId);
        validatePermission(comment, userId);
        comment.softdelete();
        log.info("댓글 삭제 완료 - commentId: {}, userId: {}", commentId, userId);
    }

    @Transactional
    public void update(Long userId, Long postId, Long commentId, CommentUpdateRequest request) {
        Comment comment = commentRepository.findByIdAndDeletedFalse(commentId)
                .orElseThrow(CommentNotFoundException::new);

        validateCommentBelongsToPost(comment, postId);
        validatePermission(comment, userId);
        comment.update(request.content());

        log.info("댓글 수정 완료 - commentId: {}, userId: {}", commentId, userId);
    }

    private void validateCommentBelongsToPost(Comment comment, Long postId) {
        if (!comment.getPost().getId().equals(postId)) {
            throw new InvalidParentCommentException();
        }
    }

    private void validatePermission(Comment comment, Long userId) {
        if (!comment.getUser().getId().equals(userId)) {
            throw new CommentAccessDeniedException();
        }
    }

    private Comment getValidParentComment(Long parentId, Long postId) {
        if (parentId == null || parentId == 0L) {
            return null;
        }

        Comment parent = commentRepository.findById(parentId)
                .orElseThrow(CommentNotFoundException::new);

        if (!Objects.equals(parent.getPost().getId(), postId)) {
            throw new InvalidParentCommentException();
        }

        return parent;
    }
}
