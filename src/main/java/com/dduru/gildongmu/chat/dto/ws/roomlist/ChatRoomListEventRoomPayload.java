package com.dduru.gildongmu.chat.dto.ws.roomlist;

import com.dduru.gildongmu.chat.domain.enums.ChatRoomStatus;
import com.dduru.gildongmu.chat.domain.enums.ChatRoomType;
import com.dduru.gildongmu.chat.dto.response.ChatRoomLastMessageResponse;
import com.dduru.gildongmu.chat.dto.response.ChatRoomListItemResponse;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

/**
 * 채팅방 목록 실시간 이벤트 안에 포함되는 채팅방 item 표현이다.
 * <p>
 * {@code UPSERT}에서는 REST 목록 item과 동일한 값을 담고, {@code REMOVE}에서는 제거 처리에 필요한 {@code chatRoomId},
 * {@code roomType}만 담는다.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ChatRoomListEventRoomPayload(
        Long chatRoomId,
        ChatRoomType roomType,
        ChatRoomStatus status,
        String displayName,
        String postTitle,
        String thumbnailUrl,
        Long postId,
        Long journeyId,
        Integer participantCount,
        ChatRoomLastMessageResponse lastMessage,
        Long unreadCount,
        Long lastReadMessageId,
        LocalDateTime activityAt,
        LocalDateTime createdAt
) {
    public static ChatRoomListEventRoomPayload from(ChatRoomListItemResponse chatRoom) {
        return new ChatRoomListEventRoomPayload(
                chatRoom.chatRoomId(),
                chatRoom.roomType(),
                chatRoom.status(),
                chatRoom.displayName(),
                chatRoom.postTitle(),
                chatRoom.thumbnailUrl(),
                chatRoom.postId(),
                chatRoom.journeyId(),
                chatRoom.participantCount(),
                chatRoom.lastMessage(),
                chatRoom.unreadCount(),
                chatRoom.lastReadMessageId(),
                chatRoom.activityAt(),
                chatRoom.createdAt()
        );
    }

    public static ChatRoomListEventRoomPayload removed(Long chatRoomId, ChatRoomType roomType) {
        return new ChatRoomListEventRoomPayload(
                chatRoomId,
                roomType,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }
}
