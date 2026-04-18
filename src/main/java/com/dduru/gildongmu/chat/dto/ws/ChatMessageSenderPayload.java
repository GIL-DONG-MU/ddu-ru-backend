package com.dduru.gildongmu.chat.dto.ws;

import com.dduru.gildongmu.profile.dto.response.ProfileImageInfo;
import com.dduru.gildongmu.profile.utils.ProfileImageResolver;
import com.dduru.gildongmu.user.domain.User;

public record ChatMessageSenderPayload(
        Long id,
        String name,
        ProfileImageInfo profileImageInfo,
        boolean isHost
) {

    public static ChatMessageSenderPayload from(User sender, Long hostUserId, ProfileImageResolver profileImageResolver) {
        return new ChatMessageSenderPayload(
                sender.getId(),
                sender.getName(),
                ProfileImageInfo.from(sender.getProfile(), profileImageResolver),
                sender.getId().equals(hostUserId)
        );
    }
}
