package com.dduru.gildongmu.chat.dto.ws;

import com.dduru.gildongmu.chat.system.ChatSystemMessageType;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "WebSocket 시스템 메시지 payload")
public record ChatSystemMessagePayload(
        @Schema(description = "시스템 메시지 타입", example = "USER_INVITED", allowableValues = {"USER_INVITED", "USER_LEFT", "USER_KICKED", "ROOM_CLOSED"})
        ChatSystemMessageType type,

        @Schema(description = "액션 수행자 회원 ID", example = "1", nullable = true)
        Long actorUserId,

        @Schema(description = "초대된 회원 ID", example = "33", nullable = true)
        Long inviteeUserId,

        @Schema(description = "채팅방을 나간 회원 ID", example = "33", nullable = true)
        Long userId,

        @Schema(description = "내보내진 회원 ID", example = "33", nullable = true)
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
