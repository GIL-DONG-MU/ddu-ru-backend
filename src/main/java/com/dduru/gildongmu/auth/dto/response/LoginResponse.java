package com.dduru.gildongmu.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record LoginResponse(
        @Schema(description = "API 인증에 사용할 Access Token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String accessToken,
        @Schema(description = "Access Token 재발급에 사용할 Refresh Token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String refreshToken,
        @Schema(description = "이번 로그인에서 새로 가입된 사용자 여부. true이면 추가 온보딩이 필요할 수 있습니다.", example = "false")
        Boolean isNewUser
) {
    public static LoginResponse of(String accessToken, String refreshToken, Boolean isNewUser) {
        return new LoginResponse(
                accessToken,
                refreshToken,
                isNewUser
        );
    }
}
