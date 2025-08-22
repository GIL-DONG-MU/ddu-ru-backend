package com.dduru.gildongmu.post.controller;

import com.dduru.gildongmu.common.annotation.CurrentUser;
import com.dduru.gildongmu.post.dto.*;
import com.dduru.gildongmu.post.service.PostQueryService;
import com.dduru.gildongmu.post.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/posts")
public class PostController implements PostApiDocs {
    private final PostService postService;
    private final PostQueryService postQueryService;

    @Override
    @GetMapping
    public ResponseEntity<PostListResponse> retrievePostsWithFilter(
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String preferredGender,
            @RequestParam(required = false) String preferredAge,
            @RequestParam(required = false) Long destinationId,
            @RequestParam(required = false) Boolean isRecruitOpen
    ) {
        PostListRequest request = new PostListRequest(
                cursor, size, keyword, startDate, endDate, preferredGender,
                preferredAge, destinationId, isRecruitOpen
        );

        PostListResponse response = postQueryService.retrieveAllWithFilter(request);

        return ResponseEntity.ok(response);
    }

    @Override
    @GetMapping("/{postId}")
    public ResponseEntity<PostDetailResponse> getPostDetail(@PathVariable Long postId) {

        PostDetailResponse response = postQueryService.retrieveDetailWithViewCount(postId);

        return ResponseEntity.ok(response);
    }

    @Override
    @PostMapping
    public ResponseEntity<PostCreateResponse> createPost(
            @CurrentUser Long userId,
            @Valid @RequestBody PostCreateRequest request
    ) {
        PostCreateResponse response = postService.create(userId, request);
        return ResponseEntity.created(URI.create("/api/v1/posts/" + response.id())).body(response);
    }

    @Override
    @PatchMapping("/{postId}")
    public ResponseEntity<Void> updatePost(
            @PathVariable Long postId,
            @CurrentUser Long userId,
            @Valid @RequestBody PostUpdateRequest request
    ) {
        postService.update(postId, userId, request);
        return ResponseEntity.noContent().build();
    }

    @Override
    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long postId,
            @CurrentUser Long userId
    ) {
        postService.delete(postId, userId);
        return ResponseEntity.noContent().build();
    }
}
