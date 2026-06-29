package com.dduru.gildongmu.verification.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
public record VerificationVerifyResponse(
        @Schema(description = "검증 결과 상태", example = "VERIFIED", allowableValues = {"VERIFIED"})
        String status,
        @Schema(description = "프로필 초기 설정 시 휴대폰 인증 완료 증명으로 전달할 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String token
) {
    public static VerificationVerifyResponse verified(String token) {
        return VerificationVerifyResponse.builder()
                .status("VERIFIED")
                .token(token)
                .build();
    }
}
