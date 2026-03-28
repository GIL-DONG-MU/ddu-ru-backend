package com.dduru.gildongmu.comment.controller;

import com.dduru.gildongmu.comment.dto.request.CommentCreateRequest;
import com.dduru.gildongmu.comment.dto.response.CommentResponse;
import com.dduru.gildongmu.comment.dto.request.CommentUpdateRequest;
import com.dduru.gildongmu.comment.service.CommentQueryService;
import com.dduru.gildongmu.comment.service.CommentService;
import com.dduru.gildongmu.common.annotation.CurrentUser;
import com.dduru.gildongmu.common.dto.ApiResult;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Hidden
@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class CommentController implements CommentApiDocs {

    private final CommentService commentService;
    private final CommentQueryService commentQueryService;

    @Override
    @PostMapping("/{postId}/comments")
    public ResponseEntity<ApiResult<CommentResponse>> createComment(
            @CurrentUser Long userId,
            @PathVariable Long postId,
            @Valid @RequestBody CommentCreateRequest request) {
        CommentResponse response = commentService.create(userId, postId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResult.created(response));
    }

    @Override
    @GetMapping("/{postId}/comments")
    public ResponseEntity<ApiResult<List<CommentResponse>>> retrieveComments(@PathVariable Long postId) {
        List<CommentResponse> comments = commentQueryService.retrieve(postId);
        return ResponseEntity.ok(ApiResult.ok(comments));
    }

    @Override
    @DeleteMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<ApiResult<Void>> deleteComment(
            @CurrentUser Long userId,
            @PathVariable Long postId,
            @PathVariable Long commentId) {
        commentService.delete(userId, postId, commentId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResult.noContent());
    }

    @Override
    @PatchMapping("/{postId}/comments/{commentId}")
    public ResponseEntity<ApiResult<Void>> updateComment(
            @CurrentUser Long userId,
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @Valid @RequestBody CommentUpdateRequest request) {
        commentService.update(userId, postId, commentId, request);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResult.noContent());
    }
}
