package com.dduru.gildongmu.admin.post.controller;

import com.dduru.gildongmu.admin.post.dto.response.AdminPostDetailResponse;
import com.dduru.gildongmu.admin.post.service.AdminPostService;
import com.dduru.gildongmu.common.annotation.CurrentUser;
import com.dduru.gildongmu.common.dto.ApiResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/posts")
public class AdminPostController implements AdminPostApiDocs {

    private final AdminPostService adminPostService;

    @Override
    @GetMapping("/{postId}")
    public ResponseEntity<ApiResult<AdminPostDetailResponse>> getPost(@PathVariable Long postId) {
        AdminPostDetailResponse response = adminPostService.getDetail(postId);
        return ResponseEntity.ok(ApiResult.ok(response));
    }

    @Override
    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResult<Void>> deletePost(
            @PathVariable Long postId,
            @CurrentUser Long adminUserId
    ) {
        adminPostService.delete(postId, adminUserId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResult.noContent());
    }
}
