package com.dduru.gildongmu.comment.controller;

import com.dduru.gildongmu.comment.dto.CommentCreateRequest;
import com.dduru.gildongmu.comment.dto.CommentResponse;
import com.dduru.gildongmu.comment.service.CommentService;
import com.dduru.gildongmu.common.annotation.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CommentController implements CommentApiDocs {

    private final CommentService commentService;

    @Override
    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<CommentResponse> createComment(@CurrentUser Long userId, @PathVariable Long postId, @Valid @RequestBody CommentCreateRequest request) {
        CommentResponse response = commentService.createComment(userId, postId, request);
        return ResponseEntity.created(URI.create("/api/v1/comments/" + response.id())).body(response);
    }
}
