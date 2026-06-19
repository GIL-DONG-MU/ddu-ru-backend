package com.dduru.gildongmu.chat.dto.query;

import com.dduru.gildongmu.chat.exception.InvalidChatRoomListCursorException;

import java.time.LocalDateTime;

public record ChatRoomListCursor(
        LocalDateTime activityAt,
        Long chatRoomId
) {
    public ChatRoomListCursor {
        if (activityAt == null || chatRoomId == null) {
            throw new InvalidChatRoomListCursorException();
        }
    }
}
