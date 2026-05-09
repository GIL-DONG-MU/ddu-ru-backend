package com.dduru.gildongmu.chat.dto.response;

import com.dduru.gildongmu.chat.domain.enums.ChatRoomType;

public record ChatRoomInfoResponse(
        Long chatRoomId,
        ChatRoomType roomType,
        boolean isActive,
        String postTitle,
        String opponentNickname,
        String journeyTitle,
        Integer memberCount
) {
}
