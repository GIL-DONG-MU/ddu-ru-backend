package com.dduru.gildongmu.auth.dto;

import lombok.Builder;

@Builder
public record LoginResponse(
        String accessToken,
        String refreshToken
) {
    public static LoginResponse of(String accessToken, String refreshToken) {
        return new LoginResponse(
                accessToken,
                refreshToken
        );
    }
}
