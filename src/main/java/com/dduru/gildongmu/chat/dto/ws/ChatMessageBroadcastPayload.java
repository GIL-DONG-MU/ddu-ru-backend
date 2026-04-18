package com.dduru.gildongmu.chat.dto.ws;

import com.dduru.gildongmu.chat.domain.enums.ChatMessageType;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

/**
 * 채팅방 토픽 구독자에게 브로드캐스트되는 메시지 페이로드.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ChatMessageBroadcastPayload(
        Long messageId,
        Long roomId,
        String postTitle,
        int recruitCount,
        int recruitCapacity,
        ChatMessageType messageType,
        ChatMessageSenderPayload sender,
        LocalDateTime createdAt
) {
}
