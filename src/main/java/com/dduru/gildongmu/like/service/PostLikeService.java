package com.dduru.gildongmu.like.service;

import com.dduru.gildongmu.auth.exception.UserNotFoundException;
import com.dduru.gildongmu.like.domain.PostLike;
import com.dduru.gildongmu.like.repository.PostLikeRepository;
import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.post.exception.PostNotFoundException;
import com.dduru.gildongmu.post.repository.PostRepository;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostLikeService {
    private final PostLikeRepository postLikeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional
    public void togglePostLike(Long userId, Long postId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> UserNotFoundException.of(userId));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> PostNotFoundException.of(postId));

        Optional<PostLike> existingLike = postLikeRepository.findByUserIdAndPostId(userId, postId);

        if (existingLike.isPresent()) {
            postLikeRepository.delete(existingLike.get());
            post.decreaseLikes();
        } else {
            postLikeRepository.save(PostLike.createPostLike(user, post));
            post.increaseLikes();
        }
    }
}
