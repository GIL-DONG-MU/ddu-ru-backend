package com.dduru.gildongmu.chat.dto.response;

import com.dduru.gildongmu.chat.domain.enums.ChatMessageType;

import java.time.LocalDateTime;

public record ChatRoomLastMessageResponse(
        Long messageId,
        ChatMessageType messageType,
        String content,
        Long senderId,
        String senderNickname,
        LocalDateTime createdAt
) {
}
