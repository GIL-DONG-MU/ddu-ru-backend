package com.dduru.gildongmu.user.dto;

import com.dduru.gildongmu.profile.domain.Profile;
import com.dduru.gildongmu.profile.domain.enums.ProfileImageType;
import com.dduru.gildongmu.user.domain.User;

public record UserInfo(
        Long id,
        String name,
        String profileImageType,
        String profileImage,
        String avatarType,
        String bgColorHex,
        String gender,
        String birthday,
        String nickname
) {
    public static UserInfo from(User user) {
        Profile profile = user.getProfile();
        ProfileImageType imageType = profile.getProfileImageType();

        String profileImage = null;
        String avatarType = null;
        String bgColorHex = null;

        if (imageType == ProfileImageType.UPLOADED) {
            profileImage = profile.getUploadedImageUrl();
        } else if (imageType == ProfileImageType.AVATAR) {
            avatarType = profile.getAvatar() != null
                    ? profile.getAvatar().getAvatarType().name()
                    : null;
            bgColorHex = profile.getBgColor() != null
                    ? profile.getBgColor().getHexCode()
                    : null;
        }

        return new UserInfo(
                user.getId(),
                user.getName(),
                imageType != null ? imageType.name() : null,
                profileImage,
                avatarType,
                bgColorHex,
                profile.getGender().name(),
                profile.getBirthday().toString(),
                profile.getNickname()
        );
    }
}
