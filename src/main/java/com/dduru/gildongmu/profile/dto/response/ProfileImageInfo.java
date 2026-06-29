package com.dduru.gildongmu.profile.dto.response;

import com.dduru.gildongmu.profile.domain.Profile;
import com.dduru.gildongmu.profile.domain.enums.ProfileImageType;
import com.dduru.gildongmu.profile.utils.ProfileImageResolver;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "프로필 이미지 표시 정보")
public record ProfileImageInfo(
        @Schema(description = "프로필 이미지 타입. AVATAR이면 bgColorId를 함께 사용하고, UPLOADED이면 url을 그대로 표시합니다.", example = "AVATAR", allowableValues = {"AVATAR", "UPLOADED", "DEFAULT"})
        ProfileImageType type,
        @Schema(description = "표시할 프로필 이미지 URL", example = "https://cdn.example.com/profiles/avatar.png", nullable = true)
        String url,
        @Schema(description = "아바타 배경색 ID. type=AVATAR일 때만 내려갑니다.", example = "3", nullable = true)
        Long bgColorId
) {

    public static ProfileImageInfo from(Profile profile, ProfileImageResolver profileImageResolver) {
        if (profile == null) {
            return null;
        }

        return new ProfileImageInfo(
                profile.getProfileImageType(),
                profileImageResolver.resolve(profile),
                resolveBgColorId(
                        profile.getProfileImageType(),
                        profile.getBgColor() != null ? profile.getBgColor().getId() : null
                )
        );
    }

    public static ProfileImageInfo from(
            ProfileImageType profileImageType,
            String uploadedImageUrl,
            String avatarImageUrl,
            Long bgColorId,
            ProfileImageResolver profileImageResolver
    ) {
        return new ProfileImageInfo(
                profileImageType,
                profileImageResolver.resolve(profileImageType, uploadedImageUrl, avatarImageUrl),
                resolveBgColorId(profileImageType, bgColorId)
        );
    }

    private static Long resolveBgColorId(ProfileImageType profileImageType, Long bgColorId) {
        return profileImageType == ProfileImageType.AVATAR ? bgColorId : null;
    }
}
