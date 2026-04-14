package com.dduru.gildongmu.post.dto.response;

import com.dduru.gildongmu.profile.domain.Profile;
import com.dduru.gildongmu.profile.domain.enums.ProfileImageType;
import com.dduru.gildongmu.profile.service.ProfileImageResolver;
import com.dduru.gildongmu.survey.domain.enums.AvatarType;
import com.dduru.gildongmu.user.domain.User;

public record ParticipantInfo(
        Long userId,
        String nickname,
        ProfileImageType profileImageType,
        String profileImage,
        AvatarType avatarType,
        String bgColorHex,
        boolean isHost
) {
    public static ParticipantInfo from(User user, boolean isHost, ProfileImageResolver profileImageResolver) {
        Profile profile = user.getProfile();
        return new ParticipantInfo(
                user.getId(),
                profile.getNickname(),
                profile.getProfileImageType(),
                profileImageResolver.resolve(profile),
                profile.getAvatar() != null ? profile.getAvatar().getAvatarType() : null,
                profile.getBgColor() != null ? profile.getBgColor().getHexCode() : null,
                isHost
        );
    }
}
