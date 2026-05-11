package com.dduru.gildongmu.chat.dto.response;

import com.dduru.gildongmu.user.domain.User;

public record ChatMessageSenderResponse(
        Long userId,
        String nickname,
        boolean isHost
) {
    public static ChatMessageSenderResponse from(User sender, Long hostUserId) {
        if (sender == null) {
            return null;
        }
        return new ChatMessageSenderResponse(
                sender.getId(),
                displayNameOf(sender),
                sender.getId().equals(hostUserId)
        );
    }

    public static String displayNameOf(User user) {
        if (user.getProfile() != null
                && user.getProfile().getNickname() != null
                && !user.getProfile().getNickname().isBlank()) {
            return user.getProfile().getNickname();
        }
        return user.getName();
    }
}
