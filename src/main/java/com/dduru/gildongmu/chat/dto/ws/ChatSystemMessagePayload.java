package com.dduru.gildongmu.chat.dto.ws;

import com.dduru.gildongmu.chat.system.ChatSystemMessageType;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ChatSystemMessagePayload(
        ChatSystemMessageType type,
        Long actorUserId,
        Long inviteeUserId,
        Long userId,
        Long targetUserId
) {

    public static ChatSystemMessagePayload userInvited(long inviteeUserId, long actorUserId) {
        return new ChatSystemMessagePayload(ChatSystemMessageType.USER_INVITED, actorUserId, inviteeUserId, null, null);
    }

    public static ChatSystemMessagePayload userLeft(long userId) {
        return new ChatSystemMessagePayload(ChatSystemMessageType.USER_LEFT, null, null, userId, null);
    }

    public static ChatSystemMessagePayload userKicked(long targetUserId, long actorUserId) {
        return new ChatSystemMessagePayload(ChatSystemMessageType.USER_KICKED, actorUserId, null, null, targetUserId);
    }

    public static ChatSystemMessagePayload roomClosed() {
        return new ChatSystemMessagePayload(ChatSystemMessageType.ROOM_CLOSED, null, null, null, null);
    }
}
