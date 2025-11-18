package com.dduru.gildongmu.like.controller;

import com.dduru.gildongmu.common.annotation.CurrentUser;
import com.dduru.gildongmu.like.service.PostLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/posts")
public class PostLikeController implements PostLikeApiDocs {
    private final PostLikeService postLikeService;

    @PostMapping("/{postId}/likes")
    public ResponseEntity<Void> togglePostLike(@CurrentUser Long userId, @PathVariable Long postId) {
        postLikeService.togglePostLike(userId, postId);
        return ResponseEntity.noContent().build();
    }
}
