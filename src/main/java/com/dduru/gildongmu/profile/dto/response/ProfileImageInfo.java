package com.dduru.gildongmu.profile.dto.response;

import com.dduru.gildongmu.profile.domain.Profile;
import com.dduru.gildongmu.profile.domain.enums.ProfileImageType;
import com.dduru.gildongmu.profile.utils.ProfileImageResolver;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "프로필 이미지 표시 정보")
public record ProfileImageInfo(
        ProfileImageType type,
        String url,
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
