package com.dduru.gildongmu.like.controller;

import com.dduru.gildongmu.common.annotation.CurrentUser;
import com.dduru.gildongmu.like.service.CommentLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/comments")
public class CommentLikeController implements CommentLikeApiDocs {
    private final CommentLikeService commentLikeService;

    @PostMapping("/{commentId}/likes")
    public ResponseEntity<Void> toggleCommentLike(@CurrentUser Long userId, @PathVariable Long commentId) {
        commentLikeService.toggleLike(userId, commentId);
        return ResponseEntity.noContent().build();
    }
}
