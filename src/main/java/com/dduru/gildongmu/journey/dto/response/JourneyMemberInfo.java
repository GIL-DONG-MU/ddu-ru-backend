package com.dduru.gildongmu.journey.dto.response;

import com.dduru.gildongmu.journey.domain.JourneyMember;
import com.dduru.gildongmu.profile.domain.Profile;
import com.dduru.gildongmu.profile.domain.enums.Gender;
import com.dduru.gildongmu.profile.dto.response.ProfileImageInfo;
import com.dduru.gildongmu.profile.utils.AgeGroupCalculator;
import com.dduru.gildongmu.profile.utils.ProfileImageResolver;
import com.dduru.gildongmu.user.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

public record JourneyMemberInfo(
        @Schema(description = "여행 멤버 회원 ID", example = "33")
        Long userId,
        @Schema(description = "닉네임", example = "여행메이트")
        String nickname,
        @Schema(description = "프로필 이미지 정보")
        ProfileImageInfo profileImage,
        @Schema(description = "호스트 여부", example = "false")
        boolean isHost,
        @Schema(description = "성별", example = "F", allowableValues = {"M", "F"})
        Gender gender,
        @Schema(description = "연령대. birthday 미설정 시 null", example = "20대", nullable = true)
        String ageGroup,
        @Schema(description = "역할 목록")
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
