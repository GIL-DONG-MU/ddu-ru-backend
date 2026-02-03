package com.dduru.gildongmu.auth.dto;

public record TokenPair(
        String accessToken,
        String refreshToken
) {
}
