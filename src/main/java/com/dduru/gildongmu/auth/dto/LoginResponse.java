package com.dduru.gildongmu.auth.dto;

import lombok.Builder;

@Builder
public record LoginResponse(
        String accessToken,
        String refreshToken,
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
