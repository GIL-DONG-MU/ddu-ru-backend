package com.dduru.gildongmu.comment.service;

import com.dduru.gildongmu.comment.domain.Comment;
import com.dduru.gildongmu.comment.dto.CommentCreateRequest;
import com.dduru.gildongmu.comment.dto.CommentUpdateRequest;

import com.dduru.gildongmu.comment.dto.CommentResponse;
import com.dduru.gildongmu.comment.exception.CommentAccessDeniedException;
import com.dduru.gildongmu.comment.exception.CommentNotFoundException;
import com.dduru.gildongmu.comment.exception.InvalidParentCommentException;
import com.dduru.gildongmu.comment.repository.CommentRepository;
import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.post.repository.PostRepository;
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

    @Transactional
    public CommentResponse create(Long userId, Long postId, CommentCreateRequest request) {
        log.debug("댓글 생성 시작 - userId: {}, postId: {}, request: {}", userId, postId, request);
        User user = userRepository.getByIdOrThrow(userId);
        Post post = postRepository.getActiveByIdOrThrow(postId);
        Comment parent = getValidParentComment(request.parentId(), postId);

        Comment comment = Comment.createComment(request.content(), user, post, parent);
        Comment savedComment = commentRepository.save(comment);

        log.info("댓글 생성 완료 - commentId: {}, userId: {}, postId: {}", savedComment.getId(), userId, postId);
        return CommentResponse.from(savedComment);
    }

    @Transactional
    public void delete(Long userId, Long commentId) {
        log.debug("댓글 삭제 시작 - userId: {}, commentId: {}", userId, commentId);
        Comment comment = commentRepository.findByIdAndDeletedFalse(commentId)
                .orElseThrow(() -> CommentNotFoundException.of(commentId));

        validatePermission(comment, userId);
        comment.softdelete();
        log.info("댓글 삭제 완료 - commentId: {}, userId: {}", commentId, userId);
    }

    @Transactional
    public CommentResponse update(Long userId, Long commentId, CommentUpdateRequest request) {
        log.debug("댓글 수정 시작 - userId: {}, commentId: {}, request: {}", userId, commentId, request);
        Comment comment = commentRepository.findByIdAndDeletedFalse(commentId)
                .orElseThrow(() -> CommentNotFoundException.of(commentId));

        validatePermission(comment, userId);
        comment.update(request.content());

        log.info("댓글 수정 완료 - commentId: {}, userId: {}", commentId, userId);
        return CommentResponse.from(comment);
    }

    private void validatePermission(Comment comment, Long userId) {
        if (!comment.getUser().getId().equals(userId)) {
            log.warn("댓글 권한 없음 - commentId: {}, userId: {}, ownerId: {}", comment.getId(), userId, comment.getUser().getId());
            throw CommentAccessDeniedException.ownerOnly();
        }
    }

    private Comment getValidParentComment(Long parentId, Long postId) {
        if (parentId == null || parentId == 0L) {
            return null;
        }

        Comment parent = commentRepository.findById(parentId)
                .orElseThrow(() -> CommentNotFoundException.of(parentId));

        if (!Objects.equals(parent.getPost().getId(), postId)) {
            throw InvalidParentCommentException.of(postId, parent.getPost().getId());
        }

        return parent;
    }
}
