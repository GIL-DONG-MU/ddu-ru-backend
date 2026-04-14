package com.dduru.gildongmu.post.dto.response;

import com.dduru.gildongmu.profile.domain.Profile;
import com.dduru.gildongmu.profile.dto.response.ProfileImageInfo;
import com.dduru.gildongmu.profile.service.ProfileImageResolver;
import com.dduru.gildongmu.user.domain.User;

public record ParticipantInfo(
        Long userId,
        String nickname,
        ProfileImageInfo profileImage,
        boolean isHost
) {
    public static ParticipantInfo from(User user, boolean isHost, ProfileImageResolver profileImageResolver) {
        Profile profile = user.getProfile();
        ProfileImageInfo profileImage = ProfileImageInfo.from(profile, profileImageResolver);

        return new ParticipantInfo(
                user.getId(),
                profile.getNickname(),
                profileImage,
                isHost
        );
    }
}
