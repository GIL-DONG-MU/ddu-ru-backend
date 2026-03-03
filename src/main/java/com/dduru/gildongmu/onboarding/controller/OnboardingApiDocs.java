package com.dduru.gildongmu.onboarding.controller;

import com.dduru.gildongmu.common.annotation.ApiErrorResponses;
import com.dduru.gildongmu.common.dto.ApiResult;
import com.dduru.gildongmu.common.exception.ErrorCode;
import com.dduru.gildongmu.onboarding.dto.response.OnboardingStatusResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Onboarding", description = "온보딩 API")
@SecurityRequirement(name = "JWT")
public interface OnboardingApiDocs {

    @Operation(summary = "온보딩 상태 조회", description = "회원가입, 기본정보 입력, 설문조사, 프로필 완성 여부를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiErrorResponses({
            ErrorCode.UNAUTHORIZED,
            ErrorCode.USER_ONBOARDING_NOT_FOUND
    })
    ResponseEntity<ApiResult<OnboardingStatusResponse>> getOnboardingStatus(@Parameter(hidden = true) Long userId);
}