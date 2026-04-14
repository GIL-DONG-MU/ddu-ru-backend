package com.dduru.gildongmu.post.controller;

import com.dduru.gildongmu.common.annotation.CurrentUser;
import com.dduru.gildongmu.common.dto.ApiResult;
import com.dduru.gildongmu.post.dto.request.PostCreateRequest;
import com.dduru.gildongmu.post.dto.request.PostListRequest;
import com.dduru.gildongmu.post.dto.request.PostStatusUpdateRequest;
import com.dduru.gildongmu.post.dto.request.PostUpdateRequest;
import com.dduru.gildongmu.post.dto.response.PostCreateResponse;
import com.dduru.gildongmu.post.dto.response.PostDetailResponse;
import com.dduru.gildongmu.post.dto.response.PostListResponse;
import com.dduru.gildongmu.post.service.PostQueryService;
import com.dduru.gildongmu.post.service.PostService;
import com.dduru.gildongmu.superhost.dto.response.MySuperHostStatusResponse;
import com.dduru.gildongmu.superhost.dto.response.SuperHostApplyResponse;
import com.dduru.gildongmu.superhost.dto.response.SuperHostPostListResponse;
import com.dduru.gildongmu.superhost.service.SuperHostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/posts")
public class PostController implements PostApiDocs {
    private final PostService postService;
    private final PostQueryService postQueryService;
    private final SuperHostService superHostService;

    @Override
    @GetMapping
    public ResponseEntity<ApiResult<PostListResponse>> retrievePosts(PostListRequest request) {
        PostListResponse response = postQueryService.retrieveAllWithFilter(request);
        return ResponseEntity.ok(ApiResult.ok(response));
    }

    @Override
    @GetMapping("/super-hosts")
    public ResponseEntity<ApiResult<SuperHostPostListResponse>> retrieveSuperHostPosts(
            @RequestParam(required = false) Integer size
    ) {
        SuperHostPostListResponse response = superHostService.retrieveSuperHostPosts(size);
        return ResponseEntity.ok(ApiResult.ok(response));
    }

    @Override
    @GetMapping("/super-hosts/me")
    public ResponseEntity<ApiResult<MySuperHostStatusResponse>> retrieveMySuperHostStatus(
            @CurrentUser Long userId
    ) {
        MySuperHostStatusResponse response = superHostService.getMyStatus(userId);
        return ResponseEntity.ok(ApiResult.ok(response));
    }

    @Override
    @GetMapping("/{postId}")
    public ResponseEntity<ApiResult<PostDetailResponse>> retrievePostDetail(
            @PathVariable Long postId,
            @CurrentUser Long userId
    ) {
        PostDetailResponse response = postService.recordViewAndGetDetail(postId, userId);
        return ResponseEntity.ok(ApiResult.ok(response));
    }

    @Override
    @PostMapping
    public ResponseEntity<ApiResult<PostCreateResponse>> createPost(
            @CurrentUser Long userId,
            @Valid @RequestBody PostCreateRequest request
    ) {
        PostCreateResponse response = postService.create(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResult.created(response));
    }

    @Override
    @PatchMapping(value = "/{postId}")
    public ResponseEntity<ApiResult<Void>> updatePost(
            @PathVariable Long postId,
            @CurrentUser Long userId,
            @Valid @RequestBody PostUpdateRequest request
    ) {
        postService.update(postId, userId, request);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResult.noContent());
    }

    @Override
    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResult<Void>> deletePost(
            @PathVariable Long postId,
            @CurrentUser Long userId
    ) {
        postService.delete(postId, userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResult.noContent());
    }

    @Override
    @PatchMapping("/{postId}/status")
    public ResponseEntity<ApiResult<Void>> updatePostStatus(
            @PathVariable Long postId,
            @CurrentUser Long userId,
            @Valid @RequestBody PostStatusUpdateRequest request
    ) {
        postService.changeStatus(postId, userId, request);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResult.noContent());
    }

    @Override
    @PostMapping("/{postId}/super-host")
    public ResponseEntity<ApiResult<SuperHostApplyResponse>> applySuperHostTicket(
            @PathVariable Long postId,
            @CurrentUser Long userId
    ) {
        SuperHostApplyResponse response = superHostService.applyTicketToPost(userId, postId);
        return ResponseEntity.ok(ApiResult.ok(response));
    }
}
