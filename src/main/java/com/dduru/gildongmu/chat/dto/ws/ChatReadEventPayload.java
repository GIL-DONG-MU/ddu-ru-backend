package com.dduru.gildongmu.chat.dto.ws;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "WebSocket 읽음 이벤트 payload")
public record ChatReadEventPayload(
        @Schema(description = "이벤트 타입. 읽음 이벤트는 항상 READ", example = "READ")
        String eventType,

        @Schema(description = "채팅방 ID", example = "10")
        Long roomId,

        @Schema(description = "읽음 처리한 회원 ID", example = "33")
        Long readerUserId,

        @Schema(description = "해당 회원이 읽은 마지막 메시지 ID", example = "120")
        Long lastReadMessageId,

        @Schema(description = "읽음 처리 시각")
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
