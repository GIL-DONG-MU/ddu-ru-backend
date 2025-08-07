package com.dduru.gildongmu.auth.dto;

import lombok.Builder;

@Builder
public record LoginResponse(
        String accessToken,
        String refreshToken,
        String name,
        String email,
        String profileImage,
        String gender,
        String ageRange,
        String phoneNumber
) {}
