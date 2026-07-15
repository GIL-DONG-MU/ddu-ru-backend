package com.dduru.gildongmu.user.controller;

import com.dduru.gildongmu.common.annotation.CurrentUser;
import com.dduru.gildongmu.common.dto.ApiResult;
import com.dduru.gildongmu.post.dto.request.MyPagePostListRequest;
import com.dduru.gildongmu.post.dto.response.MyPagePostListResponse;
import com.dduru.gildongmu.post.service.PostQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users/me")
public class UserController implements UserApiDocs {

    private final PostQueryService postQueryService;

    @Override
    @GetMapping("/posts")
    public ResponseEntity<ApiResult<MyPagePostListResponse>> retrieveMyPosts(
            @CurrentUser Long userId,
            @Valid MyPagePostListRequest request
    ) {
        MyPagePostListResponse response = postQueryService.retrieveMyPosts(userId, request);
        return ResponseEntity.ok(ApiResult.ok(response));
    }

}
