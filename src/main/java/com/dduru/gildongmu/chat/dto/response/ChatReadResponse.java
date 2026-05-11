package com.dduru.gildongmu.chat.dto.response;

public record ChatReadResponse(
        Long chatRoomId,
        Long lastReadMessageId,
        Boolean updated
) {
}
