package com.dduru.gildongmu.profile.dto.response;

import com.dduru.gildongmu.profile.domain.Profile;
import com.dduru.gildongmu.profile.utils.AgeGroupCalculator;
import com.dduru.gildongmu.profile.utils.ProfileImageResolver;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "마이페이지 프로필 조회 응답")
public record MyProfileResponse(
        @Schema(description = "닉네임", example = "김해나")
        String nickname,

        @Schema(description = "나이대. birthday 미설정 시 null", example = "20대", nullable = true)
        String ageGroup,

        @Schema(description = "한줄소개. 미설정 시 null", example = "맛집 찾는 여행 좋아해요", nullable = true)
        String bio,

        @Schema(description = "프로필 이미지 정보")
        ProfileImageInfo profileImage
) {
    public static MyProfileResponse of(Profile profile, ProfileImageResolver profileImageResolver, LocalDate today) {
        return new MyProfileResponse(
                profile.getNickname(),
                AgeGroupCalculator.toAgeGroup(profile.getBirthday(), today),
                profile.getBio(),
                ProfileImageInfo.from(profile, profileImageResolver)
        );
    }
}
