package com.dduru.gildongmu.chat.dto.query;

import java.time.LocalDateTime;

public record ChatRoomListCursor(
        LocalDateTime activityAt,
        Long chatRoomId
) {
    public ChatRoomListCursor {
        if (activityAt == null || chatRoomId == null) {
            throw new IllegalArgumentException("cursor 형식이 올바르지 않습니다.");
        }
    }
}
