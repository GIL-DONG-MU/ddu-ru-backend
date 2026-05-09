package com.dduru.gildongmu.chat.dto.response;

import java.util.List;

public record ChatMessagesResponse(
        ChatRoomInfoResponse roomInfo,
        ChatMessagePageResponse page,
        List<ChatMessageItemResponse> messages
) {
}
