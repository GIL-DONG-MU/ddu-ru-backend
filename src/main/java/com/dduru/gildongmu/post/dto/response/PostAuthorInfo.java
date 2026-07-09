package com.dduru.gildongmu.post.dto.response;

import com.dduru.gildongmu.profile.domain.Profile;
import com.dduru.gildongmu.profile.domain.enums.Gender;
import com.dduru.gildongmu.profile.dto.response.ProfileImageInfo;
import com.dduru.gildongmu.profile.utils.AgeGroupCalculator;
import com.dduru.gildongmu.profile.utils.ProfileImageResolver;
import com.dduru.gildongmu.user.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

public record PostAuthorInfo(
        @Schema(description = "작성자 회원 ID", example = "33")
        Long userId,
        @Schema(description = "작성자 닉네임", example = "여행메이트")
        String nickname,
        @Schema(description = "작성자 프로필 이미지 정보")
        ProfileImageInfo profileImage,
        @Schema(description = "작성자 성별", example = "F", allowableValues = {"M", "F"})
        Gender gender,
        @Schema(description = "작성자 연령대. birthday 미설정 시 null", example = "20대", nullable = true)
        String ageGroup,
        @Schema(description = "작성자가 슈퍼호스트인지 여부", example = "false")
        boolean isSuperHost
) {
    public static PostAuthorInfo from(User user, boolean isSuperHost, ProfileImageResolver profileImageResolver, LocalDate today) {
        Profile profile = user.getProfile();
        return new PostAuthorInfo(
                user.getId(),
                profile.getNickname(),
                ProfileImageInfo.from(profile, profileImageResolver),
                profile.getGender(),
                AgeGroupCalculator.toAgeGroup(profile.getBirthday(), today),
                isSuperHost
        );
    }
}
