package com.dduru.gildongmu.admin.user.controller;

import com.dduru.gildongmu.admin.user.dto.response.AdminUserDetailResponse;
import com.dduru.gildongmu.admin.user.dto.response.AdminUserListResponse;
import com.dduru.gildongmu.admin.user.service.AdminUserService;
import com.dduru.gildongmu.common.dto.ApiResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/users")
public class AdminUserController implements AdminUserApiDocs {

    private final AdminUserService adminUserService;

    @Override
    @GetMapping
    public ResponseEntity<ApiResult<AdminUserListResponse>> listUsers(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        AdminUserListResponse response = adminUserService.findAll(pageable);
        return ResponseEntity.ok(ApiResult.ok(response));
    }

    @Override
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResult<AdminUserDetailResponse>> getUser(@PathVariable Long userId) {
        AdminUserDetailResponse response = adminUserService.findById(userId);
        return ResponseEntity.ok(ApiResult.ok(response));
    }
}
