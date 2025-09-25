package com.dduru.gildongmu.comment.service;

import com.dduru.gildongmu.comment.domain.Comment;
import com.dduru.gildongmu.comment.dto.CommentCreateRequest;
import com.dduru.gildongmu.comment.dto.CommentResponse;
import com.dduru.gildongmu.comment.exception.CommentNotFoundException;
import com.dduru.gildongmu.comment.exception.InvalidParentCommentException;
import com.dduru.gildongmu.comment.repository.CommentRepository;
import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.post.repository.PostRepository;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional
    public CommentResponse create(Long userId, Long postId, CommentCreateRequest request) {
        User user = userRepository.getByIdOrThrow(userId);
        Post post = postRepository.getActiveByIdOrThrow(postId);
        Comment parent = getValidParentComment(request.parentId(), postId);

        Comment comment = Comment.createComment(request.content(), user, post, parent);
        Comment savedComment = commentRepository.save(comment);
        return CommentResponse.from(savedComment);
    }

    @Transactional
    public void delete(Long commentId) {
        Comment comment = commentRepository.findByIdAndDeletedFalse(commentId)
                .orElseThrow(() -> CommentNotFoundException.of(commentId));

        comment.softdelete();
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
