package com.dduru.gildongmu.chat.dto.request;

import com.dduru.gildongmu.chat.domain.enums.ChatRoomType;

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
