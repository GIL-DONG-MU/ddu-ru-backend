package com.dduru.gildongmu.user.dto;

import com.dduru.gildongmu.profile.domain.Profile;
import com.dduru.gildongmu.profile.domain.enums.Gender;
import com.dduru.gildongmu.profile.domain.enums.ProfileImageType;
import com.dduru.gildongmu.survey.domain.enums.AvatarType;
import com.dduru.gildongmu.user.domain.User;

import java.time.LocalDate;

public record UserInfo(
        Long id,
        String name,
        ProfileImageType profileImageType,
        String profileImage,
        AvatarType avatarType,
        String bgColorHex,
        Gender gender,
        LocalDate birthday,
        String nickname
) {
    public static UserInfo from(User user) {
        Profile profile = user.getProfile();
        ProfileImageType imageType = profile.getProfileImageType();

        String profileImage = null;
        AvatarType avatarType = null;
        String bgColorHex = null;

        if (imageType == ProfileImageType.UPLOADED) {
            profileImage = profile.getUploadedImageUrl();
        } else if (imageType == ProfileImageType.AVATAR) {
            avatarType = profile.getAvatar() != null
                    ? profile.getAvatar().getAvatarType()
                    : null;
            bgColorHex = profile.getBgColor() != null
                    ? profile.getBgColor().getHexCode()
                    : null;
        }

        return new UserInfo(
                user.getId(),
                user.getName(),
                imageType,
                profileImage,
                avatarType,
                bgColorHex,
                profile.getGender(),
                profile.getBirthday(),
                profile.getNickname()
        );
    }
}
