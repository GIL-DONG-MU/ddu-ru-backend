package com.dduru.gildongmu.user.dto;

import com.dduru.gildongmu.user.domain.User;

public record UserInfo(
        Long id,
        String name,
        String profileImage,
        String gender,
        String birthday,
        String nickname
) {
    public static UserInfo from(User user) {
        return new UserInfo(
                user.getId(),
                user.getName(),
                user.getProfileImage(),
                user.getGender() != null ? user.getGender().name() : null,
                // user.getAgeRange().name(),
                user.getBirthday() != null ? user.getBirthday().toString() : null,
                user.getNickname()
        );
    }
}
