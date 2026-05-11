package com.dduru.gildongmu.chat.dto.ws;

import java.time.LocalDateTime;

public record ChatReadEventPayload(
        String eventType,
        Long roomId,
        Long readerUserId,
        Long lastReadMessageId,
        LocalDateTime readAt
) {
    private static final String READ_EVENT_TYPE = "READ";

    public static ChatReadEventPayload of(
            Long roomId,
            Long readerUserId,
            Long lastReadMessageId,
            LocalDateTime readAt
    ) {
        return new ChatReadEventPayload(
                READ_EVENT_TYPE,
                roomId,
                readerUserId,
                lastReadMessageId,
                readAt
        );
    }
}
