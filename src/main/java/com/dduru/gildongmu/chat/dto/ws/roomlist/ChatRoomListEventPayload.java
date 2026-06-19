package com.dduru.gildongmu.chat.dto.ws.roomlist;

import com.dduru.gildongmu.chat.domain.enums.ChatRoomType;
import com.dduru.gildongmu.chat.dto.response.ChatRoomListItemResponse;

import java.time.LocalDateTime;

/**
 * 클라이언트가 {@code /user/queue/chat-room-list}에서 수신하는 채팅방 목록 변경 이벤트다.
 * <p>
 * {@code UPSERT}는 완성된 채팅방 item을 포함하고, {@code REMOVE}는 목록 제거에 필요한 최소 채팅방 식별 정보만 포함한다.
 */
public record ChatRoomListEventPayload(
        ChatRoomListEventType eventType,
        ChatRoomListEventReason reason,
        ChatRoomListEventRoomPayload chatRoom,
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
