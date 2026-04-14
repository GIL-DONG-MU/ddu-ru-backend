package com.dduru.gildongmu.user.dto;

import com.dduru.gildongmu.profile.domain.Profile;
import com.dduru.gildongmu.profile.domain.enums.Gender;
import com.dduru.gildongmu.profile.dto.response.ProfileImageInfo;
import com.dduru.gildongmu.profile.service.ProfileImageResolver;
import com.dduru.gildongmu.user.domain.User;

import java.time.LocalDate;

public record UserInfo(
        Long id,
        String name,
        ProfileImageInfo profileImage,
        Gender gender,
        LocalDate birthday,
        String nickname
) {
    public static UserInfo from(User user, ProfileImageResolver profileImageResolver) {
        Profile profile = user.getProfile();
        ProfileImageInfo profileImage = ProfileImageInfo.from(profile, profileImageResolver);

        return new UserInfo(
                user.getId(),
                user.getName(),
                profileImage,
                profile.getGender(),
                profile.getBirthday(),
                profile.getNickname()
        );
    }
}
