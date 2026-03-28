package com.dduru.gildongmu.admin.user.controller;

import com.dduru.gildongmu.admin.user.dto.response.AdminUserDetailResponse;
import com.dduru.gildongmu.admin.user.dto.response.AdminUserListResponse;
import com.dduru.gildongmu.common.annotation.ApiErrorResponses;
import com.dduru.gildongmu.common.dto.ApiResult;
import com.dduru.gildongmu.common.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;

@Tag(name = "Admin Users", description = "관리자 사용자 API")
public interface AdminUserApiDocs {

    @Operation(summary = "사용자 목록 조회", description = "전체 사용자를 페이지 단위로 조회합니다.")
    @ApiErrorResponses({
            ErrorCode.UNAUTHORIZED,
            ErrorCode.ACCESS_DENIED
    })
    ResponseEntity<ApiResult<AdminUserListResponse>> listUsers(
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    );

    @Operation(summary = "사용자 상세 조회", description = "사용자 기본 정보와 프로필 요약을 조회합니다.")
    @ApiErrorResponses({
            ErrorCode.USER_NOT_FOUND,
            ErrorCode.UNAUTHORIZED,
            ErrorCode.ACCESS_DENIED
    })
    ResponseEntity<ApiResult<AdminUserDetailResponse>> getUser(
            @Parameter(description = "사용자 ID") Long userId
    );
}
