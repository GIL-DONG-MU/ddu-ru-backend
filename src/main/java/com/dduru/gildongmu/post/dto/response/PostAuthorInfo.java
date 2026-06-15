package com.dduru.gildongmu.post.dto.response;

import com.dduru.gildongmu.profile.domain.Profile;
import com.dduru.gildongmu.profile.domain.enums.Gender;
import com.dduru.gildongmu.profile.dto.response.ProfileImageInfo;
import com.dduru.gildongmu.profile.utils.ProfileImageResolver;
import com.dduru.gildongmu.user.domain.User;

import java.time.LocalDate;
import java.time.Period;

public record PostAuthorInfo(
        Long userId,
        String nickname,
        ProfileImageInfo profileImage,
        Gender gender,
        Integer ageGroup,
        boolean isSuperHost
) {
    public static PostAuthorInfo from(User user, boolean isSuperHost, ProfileImageResolver profileImageResolver, LocalDate today) {
        Profile profile = user.getProfile();
        return new PostAuthorInfo(
                user.getId(),
                profile.getNickname(),
                ProfileImageInfo.from(profile, profileImageResolver),
                profile.getGender(),
                toAgeGroup(profile.getBirthday(), today),
                isSuperHost
        );
    }

    private static Integer toAgeGroup(LocalDate birthday, LocalDate today) {
        if (birthday == null) return null;
        int age = Period.between(birthday, today).getYears();
        return (age / 10) * 10;
    }
}
