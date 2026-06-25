package com.dduru.gildongmu.chat.dto.request;

import com.dduru.gildongmu.chat.domain.enums.ChatRoomType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "채팅방 목록 필터. ALL은 전체, PRIVATE은 1:1 채팅방, GROUP은 그룹 채팅방입니다.")
public enum ChatRoomListType {
    ALL,
    PRIVATE,
    GROUP;

    public ChatRoomType toChatRoomTypeOrNull() {
        return switch (this) {
            case ALL -> null;
            case PRIVATE -> ChatRoomType.PRIVATE;
            case GROUP -> ChatRoomType.GROUP;
        };
    }
}
