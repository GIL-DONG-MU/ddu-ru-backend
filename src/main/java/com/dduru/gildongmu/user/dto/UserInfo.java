package com.dduru.gildongmu.user.dto;

import com.dduru.gildongmu.profile.domain.Profile;
import com.dduru.gildongmu.profile.domain.enums.Gender;
import com.dduru.gildongmu.profile.domain.enums.ProfileImageType;
import com.dduru.gildongmu.profile.service.ProfileImageResolver;
import com.dduru.gildongmu.survey.domain.enums.AvatarType;
import com.dduru.gildongmu.user.domain.User;

import java.time.LocalDate;

public record UserInfo(
        Long id,
        String name,
        ProfileImageType profileImageType,
        String profileImage,
        AvatarType avatarType,
        Long bgColorId,
        String bgColorHex,
        Gender gender,
        LocalDate birthday,
        String nickname
) {
    public static UserInfo from(User user, ProfileImageResolver profileImageResolver) {
        Profile profile = user.getProfile();
        ProfileImageType imageType = profile.getProfileImageType();

        String profileImage = profileImageResolver.resolve(profile);
        AvatarType avatarType = null;
        Long bgColorId = null;
        String bgColorHex = null;

        if (imageType == ProfileImageType.AVATAR) {
            avatarType = profile.getAvatar() != null
                    ? profile.getAvatar().getAvatarType()
                    : null;
            if (profile.getBgColor() != null) {
                bgColorId = profile.getBgColor().getId();
                bgColorHex = profile.getBgColor().getHexCode();
            }
        }

        return new UserInfo(
                user.getId(),
                user.getName(),
                imageType,
                profileImage,
                avatarType,
                bgColorId,
                bgColorHex,
                profile.getGender(),
                profile.getBirthday(),
                profile.getNickname()
        );
    }
}
