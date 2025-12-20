package com.dduru.gildongmu.user.dto;

import com.dduru.gildongmu.profile.domain.Profile;
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
        Profile profile = user.getProfile();
        return new UserInfo(
                user.getId(),
                user.getName(),
                profile != null ? profile.getProfileImage() : null,
                profile != null && profile.getGender() != null ? profile.getGender().name() : null,
                profile != null && profile.getBirthday() != null ? profile.getBirthday().toString() : null,
                profile != null ? profile.getNickname() : null
        );
    }
}
