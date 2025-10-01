package com.dduru.gildongmu.comment.controller;

import com.dduru.gildongmu.comment.dto.CommentCreateRequest;
import com.dduru.gildongmu.comment.dto.CommentResponse;
import com.dduru.gildongmu.comment.dto.CommentUpdateRequest;
import com.dduru.gildongmu.comment.service.CommentQueryService;
import com.dduru.gildongmu.comment.service.CommentService;
import com.dduru.gildongmu.common.annotation.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CommentController implements CommentApiDocs {

    private final CommentService commentService;
    private final CommentQueryService commentQueryService;

    @Override
    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<CommentResponse> createComment(
            @CurrentUser Long userId,
            @PathVariable Long postId,
            @Valid @RequestBody CommentCreateRequest request) {
        CommentResponse response = commentService.create(userId, postId, request);
        return ResponseEntity.created(URI.create("/api/v1/comments/" + response.id())).body(response);
    }

    @Override
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<List<CommentResponse>> retrieveComments(@PathVariable Long postId) {
        List<CommentResponse> comments = commentQueryService.retrieve(postId);
        return ResponseEntity.ok(comments);
    }

    @Override
    @DeleteMapping("/posts/{postId}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @CurrentUser Long userId,
            @PathVariable Long postId,
            @PathVariable Long commentId) {
        commentService.delete(userId, postId, commentId);
        return ResponseEntity.noContent().build();
    }

    @Override
    @PatchMapping("/posts/{postId}/comments/{commentId}")
    public ResponseEntity<Void> updateComment(
            @CurrentUser Long userId,
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @Valid @RequestBody CommentUpdateRequest request) {
        commentService.update(userId, postId, commentId, request);
        return ResponseEntity.noContent().build();
    }
}
