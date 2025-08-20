package com.dduru.gildongmu.auth.controller;

import com.dduru.gildongmu.auth.dto.LoginResponse;
import com.dduru.gildongmu.auth.dto.RefreshTokenRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;

import java.util.Map;

@Tag(name = "Authentication", description = "OAuth 인증 API")
public interface OauthApiDocs {

    @Operation(summary = "ID Token 로그인 (모바일)", description = "ID Token을 사용한 OAuth 로그인을 처리합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    ResponseEntity<LoginResponse> loginWithIdToken(
            @Parameter(description = "OAuth Provider (kakao, google)", example = "kakao") String provider,
            @Parameter(description = "ID Token") String idToken,
            Map<String, String> request
    );

    @Operation(summary = "Access Token 갱신", description = "Refresh Token을 사용하여 Access Token을 갱신합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "토큰 갱신 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 Refresh Token")
    })
    ResponseEntity<LoginResponse> refreshAccessToken(@Valid RefreshTokenRequest request);

    @Operation(summary = "로그아웃", description = "사용자 로그아웃을 처리합니다.")
    @ApiResponse(responseCode = "204", description = "로그아웃 성공")
    ResponseEntity<Void> logout(@Parameter(hidden = true) Long userId);
}
