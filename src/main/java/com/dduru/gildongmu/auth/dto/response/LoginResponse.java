package com.dduru.gildongmu.auth.dto.response;

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
