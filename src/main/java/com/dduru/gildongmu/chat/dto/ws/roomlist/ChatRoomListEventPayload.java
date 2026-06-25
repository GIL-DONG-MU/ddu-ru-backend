package com.dduru.gildongmu.chat.dto.ws.roomlist;

import com.dduru.gildongmu.chat.domain.enums.ChatRoomType;
import com.dduru.gildongmu.chat.dto.response.ChatRoomListItemResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * 클라이언트가 {@code /user/queue/chat-room-list}에서 수신하는 채팅방 목록 변경 이벤트다.
 * <p>
 * {@code UPSERT}는 완성된 채팅방 item을 포함하고, {@code REMOVE}는 목록 제거에 필요한 최소 채팅방 식별 정보만 포함한다.
 */
@Schema(description = "WebSocket 사용자별 채팅방 목록 변경 이벤트 payload")
public record ChatRoomListEventPayload(
        @Schema(description = "목록 적용 방식", example = "UPSERT", allowableValues = {"UPSERT", "REMOVE"})
        ChatRoomListEventType eventType,

        @Schema(description = "목록 변경 원인", example = "MESSAGE_CREATED", allowableValues = {"MESSAGE_CREATED", "READ_UPDATED", "ROOM_META_UPDATED", "MEMBER_CHANGED"})
        ChatRoomListEventReason reason,

        @Schema(description = "변경 대상 채팅방. UPSERT는 완성된 item, REMOVE는 chatRoomId와 roomType만 포함")
        ChatRoomListEventRoomPayload chatRoom,

        @Schema(description = "이벤트 발생 시각")
        LocalDateTime occurredAt
) {
    public static ChatRoomListEventPayload upsert(
            ChatRoomListEventReason reason,
            ChatRoomListItemResponse chatRoom,
            LocalDateTime occurredAt
    ) {
        return new ChatRoomListEventPayload(
                ChatRoomListEventType.UPSERT,
                reason,
                ChatRoomListEventRoomPayload.from(chatRoom),
                occurredAt
        );
    }

    public static ChatRoomListEventPayload remove(
            ChatRoomListEventReason reason,
            Long chatRoomId,
            ChatRoomType roomType,
            LocalDateTime occurredAt
    ) {
        return new ChatRoomListEventPayload(
                ChatRoomListEventType.REMOVE,
                reason,
                ChatRoomListEventRoomPayload.removed(chatRoomId, roomType),
                occurredAt
        );
    }
}
