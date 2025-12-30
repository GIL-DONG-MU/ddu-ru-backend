package com.dduru.gildongmu.auth.controller;

import com.dduru.gildongmu.auth.dto.LoginRequest;
import com.dduru.gildongmu.auth.dto.LoginResponse;
import com.dduru.gildongmu.auth.dto.RefreshTokenRequest;
import com.dduru.gildongmu.common.annotation.ApiErrorResponses;
import com.dduru.gildongmu.common.dto.ApiResult;
import com.dduru.gildongmu.common.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Authentication", description = "OAuth 인증 API")
public interface OauthApiDocs {

    @Operation(summary = "ID Token 로그인 (모바일)", description = "ID Token을 사용한 OAuth 로그인을 처리합니다.")
    @ApiResponse(responseCode = "200", description = "로그인 성공")
    @ApiErrorResponses({
            ErrorCode.INVALID_INPUT_VALUE,
            ErrorCode.UNSUPPORTED_SOCIAL_LOGIN,
            ErrorCode.SOCIAL_LOGIN_FAILED,
            ErrorCode.INVALID_TOKEN,
            ErrorCode.DUPLICATE_EMAIL
    })
    ResponseEntity<ApiResult<LoginResponse>> loginWithIdToken(
            @Parameter(description = "OAuth Provider (kakao, google)", example = "kakao") String provider,
            @RequestBody(required = false) LoginRequest request
    );

    @Operation(summary = "Access Token 갱신", description = "Refresh Token을 사용하여 Access Token을 갱신합니다.")
    @ApiResponse(responseCode = "200", description = "토큰 갱신 성공")
    @ApiErrorResponses({
            ErrorCode.INVALID_INPUT_VALUE,
            ErrorCode.INVALID_TOKEN,
            ErrorCode.EXPIRED_TOKEN
    })
    ResponseEntity<ApiResult<LoginResponse>> refreshAccessToken(@Valid RefreshTokenRequest request);

    @Operation(summary = "로그아웃", description = "사용자 로그아웃을 처리합니다.", security = @SecurityRequirement(name = "JWT"))
    @ApiResponse(responseCode = "204", description = "로그아웃 성공", content = @Content())
    @ApiErrorResponses({ErrorCode.UNAUTHORIZED, ErrorCode.INVALID_TOKEN, ErrorCode.EXPIRED_TOKEN})
    ResponseEntity<ApiResult<Void>> logout(@Parameter(hidden = true) Long userId);
}
