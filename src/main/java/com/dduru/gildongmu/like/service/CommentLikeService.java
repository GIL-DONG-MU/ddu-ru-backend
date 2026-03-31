package com.dduru.gildongmu.like.service;

import com.dduru.gildongmu.auth.exception.UserNotFoundException;
import com.dduru.gildongmu.comment.domain.Comment;
import com.dduru.gildongmu.comment.exception.CommentNotFoundException;
import com.dduru.gildongmu.comment.repository.CommentRepository;
import com.dduru.gildongmu.like.domain.CommentLike;
import com.dduru.gildongmu.like.repository.CommentLikeRepository;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommentLikeService {
    private final CommentLikeRepository commentLikeRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    @Transactional
    public void toggleLike(Long userId, Long commentId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(CommentNotFoundException::new);

        Optional<CommentLike> existingLike = commentLikeRepository.findByUserIdAndCommentId(userId, commentId);

        if (existingLike.isPresent()) {
            commentLikeRepository.delete(existingLike.get());
            comment.decreaseLikeCount();
        } else {
            commentLikeRepository.save(CommentLike.createLike(user, comment));
            comment.increaseLikeCount();
        }
    }
}
