package com.dduru.gildongmu.auth.dto;

import com.dduru.gildongmu.auth.domain.User;
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
) {
    public static LoginResponse of(User user, String accessToken, String refreshToken) {
        return new LoginResponse(
                accessToken,
                refreshToken,
                user.getName(),
                user.getEmail(),
                user.getProfileImage(),
                getEnumName(user.getGender()),
                getEnumName(user.getAgeRange()),
                user.getPhoneNumber()
        );
    }
    private static String getEnumName(Enum<?> enumValue) {
        if (enumValue == null) {
            return null;
        }
        return enumValue.name();
    }
}
