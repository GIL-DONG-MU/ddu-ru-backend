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
                profile.getProfileImage(),
                profile.getGender().name(),
                profile.getBirthday().toString(),
                profile.getNickname()
        );
    }
}
