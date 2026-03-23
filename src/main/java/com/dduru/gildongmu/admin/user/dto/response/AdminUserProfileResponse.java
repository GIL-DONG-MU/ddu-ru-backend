package com.dduru.gildongmu.admin.user.dto.response;

import com.dduru.gildongmu.profile.domain.Profile;
import com.dduru.gildongmu.profile.domain.enums.Gender;
import com.dduru.gildongmu.profile.domain.enums.ProfileImageType;

import java.time.LocalDate;

public record AdminUserProfileResponse(
        Gender gender,
        LocalDate birthday,
        String bio,
        ProfileImageType profileImageType
) {
    public static AdminUserProfileResponse from(Profile profile) {
        return new AdminUserProfileResponse(
                profile.getGender(),
                profile.getBirthday(),
                profile.getBio(),
                profile.getProfileImageType()
        );
    }
}
