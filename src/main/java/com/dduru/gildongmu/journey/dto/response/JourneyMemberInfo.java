package com.dduru.gildongmu.journey.dto.response;

import com.dduru.gildongmu.journey.domain.JourneyMember;
import com.dduru.gildongmu.profile.domain.Profile;
import com.dduru.gildongmu.profile.domain.enums.Gender;
import com.dduru.gildongmu.profile.dto.response.ProfileImageInfo;
import com.dduru.gildongmu.profile.utils.AgeGroupCalculator;
import com.dduru.gildongmu.profile.utils.ProfileImageResolver;
import com.dduru.gildongmu.user.domain.User;

import java.time.LocalDate;
import java.util.List;

public record JourneyMemberInfo(
        Long userId,
        String nickname,
        ProfileImageInfo profileImage,
        boolean isHost,
        Gender gender,
        String ageGroup,
        List<RoleLabelInfo> roles
) {
    public static JourneyMemberInfo from(JourneyMember member, ProfileImageResolver resolver, LocalDate today) {
        User user = member.getUser();
        Profile profile = user.getProfile();
        return new JourneyMemberInfo(
                user.getId(),
                profile.getNickname(),
                ProfileImageInfo.from(profile, resolver),
                member.isHost(),
                profile.getGender(),
                AgeGroupCalculator.toAgeGroup(profile.getBirthday(), today),
                member.getRoleLabels().stream()
                        .map(RoleLabelInfo::from)
                        .toList()
        );
    }
}
