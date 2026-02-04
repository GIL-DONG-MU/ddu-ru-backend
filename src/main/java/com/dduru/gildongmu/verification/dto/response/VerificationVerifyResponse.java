package com.dduru.gildongmu.verification.dto.response;

import lombok.Builder;

@Builder
public record VerificationVerifyResponse(
        String status,
        String token
) {
    public static VerificationVerifyResponse verified(String token) {
        return VerificationVerifyResponse.builder()
                .status("VERIFIED")
                .token(token)
                .build();
    }
}
