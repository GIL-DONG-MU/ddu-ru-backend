package com.dduru.gildongmu.post.controller;

import com.dduru.gildongmu.common.annotation.CurrentUser;
import com.dduru.gildongmu.post.dto.PostCreateRequest;
import com.dduru.gildongmu.post.dto.PostCreateResponse;
import com.dduru.gildongmu.post.dto.PostUpdateRequest;
import com.dduru.gildongmu.post.dto.PostUpdateResponse;
import com.dduru.gildongmu.post.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/posts")
public class PostController {
    private final PostService postService;

    @PostMapping
    public ResponseEntity<PostCreateResponse> createPost(
            @CurrentUser Long userId,
            @Valid @RequestBody PostCreateRequest request) {
        PostCreateResponse response = postService.create(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{postId}")
    public ResponseEntity<PostUpdateResponse> updatePost(
            @PathVariable Long postId,
            @CurrentUser Long userId,
            @Valid @RequestBody PostUpdateRequest request){
        PostUpdateResponse response = postService.update(postId, userId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long postId,
            @CurrentUser Long userId){
        postService.delete(postId, userId);
        return ResponseEntity.noContent().build();
    }
}
