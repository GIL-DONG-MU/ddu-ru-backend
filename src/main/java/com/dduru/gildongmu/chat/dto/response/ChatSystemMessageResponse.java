package com.dduru.gildongmu.chat.dto.response;

import com.dduru.gildongmu.chat.constants.ChatMessageConstants;
import com.dduru.gildongmu.chat.dto.ws.ChatSystemMessagePayload;
import com.dduru.gildongmu.chat.system.ChatSystemMessageType;
import com.dduru.gildongmu.user.domain.User;

import java.util.Map;

public record ChatSystemMessageResponse(
        ChatSystemMessageType type,
        String displayText,
        Long actorUserId,
        Long inviteeUserId,
        Long userId,
        Long targetUserId
) {
    public static ChatSystemMessageResponse from(
            ChatSystemMessagePayload payload,
            Map<Long, User> systemUsers
    ) {
        return new ChatSystemMessageResponse(
                payload.type(),
                displayText(payload, systemUsers),
                payload.actorUserId(),
                payload.inviteeUserId(),
                payload.userId(),
                payload.targetUserId()
        );
    }

    private static String displayText(ChatSystemMessagePayload payload, Map<Long, User> systemUsers) {
        String template = payload.type().getDisplayTextTemplate();
        Long displayUserId = switch (payload.type()) {
            case USER_INVITED -> payload.inviteeUserId();
            case USER_LEFT -> payload.userId();
            case USER_KICKED -> payload.targetUserId();
            case ROOM_CLOSED -> null;
        };
        if (displayUserId == null) {
            return template;
        }
        return template.formatted(displayNickname(displayUserId, systemUsers));
    }

    private static String displayNickname(Long userId, Map<Long, User> users) {
        if (userId == null) {
            return ChatMessageConstants.UNKNOWN_NICKNAME;
        }
        User user = users.get(userId);
        if (user == null) {
            return ChatMessageConstants.UNKNOWN_NICKNAME;
        }
        return ChatMessageSenderResponse.displayNameOf(user);
    }
}
