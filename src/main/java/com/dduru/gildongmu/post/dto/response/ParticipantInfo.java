package com.dduru.gildongmu.post.dto.response;

import com.dduru.gildongmu.profile.domain.Profile;
import com.dduru.gildongmu.profile.domain.enums.Gender;
import com.dduru.gildongmu.profile.dto.response.ProfileImageInfo;
import com.dduru.gildongmu.profile.utils.AgeGroupCalculator;
import com.dduru.gildongmu.profile.utils.ProfileImageResolver;
import com.dduru.gildongmu.user.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

public record ParticipantInfo(
        @Schema(description = "참여자 회원 ID", example = "33")
        Long userId,
        @Schema(description = "참여자 닉네임", example = "여행메이트")
        String nickname,
        @Schema(description = "참여자 프로필 이미지 정보")
        ProfileImageInfo profileImage,
        @Schema(description = "게시글 호스트 여부", example = "false")
        boolean isHost,
        @Schema(description = "참여자 성별", example = "F", allowableValues = {"M", "F"})
        Gender gender,
        @Schema(description = "참여자 연령대. 20은 20대를 의미합니다.", example = "20")
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
