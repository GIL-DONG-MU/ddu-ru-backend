package com.dduru.gildongmu.post.dto.response;

import com.dduru.gildongmu.profile.domain.Profile;
import com.dduru.gildongmu.profile.domain.enums.Gender;
import com.dduru.gildongmu.profile.dto.response.ProfileImageInfo;
import com.dduru.gildongmu.profile.utils.AgeGroupCalculator;
import com.dduru.gildongmu.profile.utils.ProfileImageResolver;
import com.dduru.gildongmu.user.domain.User;

import java.time.LocalDate;

public record ParticipantInfo(
        Long userId,
        String nickname,
        ProfileImageInfo profileImage,
        boolean isHost,
        Gender gender,
        Integer ageGroup
) {
    public static ParticipantInfo from(User user, boolean isHost, ProfileImageResolver profileImageResolver, LocalDate today) {
        Profile profile = user.getProfile();
        ProfileImageInfo profileImage = ProfileImageInfo.from(profile, profileImageResolver);

        return new ParticipantInfo(
                user.getId(),
                profile.getNickname(),
                profileImage,
                isHost,
                profile.getGender(),
                AgeGroupCalculator.toAgeGroup(profile.getBirthday(), today)
        );
    }
}
