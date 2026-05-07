package com.dduru.gildongmu.chat.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class InvalidChatRoomContextException extends BusinessException {

    private InvalidChatRoomContextException(ErrorCode errorCode) {
        super(errorCode);
    }

    public static InvalidChatRoomContextException missingRoomType() {
        return new InvalidChatRoomContextException(ErrorCode.CHAT_ROOM_TYPE_REQUIRED);
    }

    public static InvalidChatRoomContextException invalidPrivateContext() {
        return new InvalidChatRoomContextException(ErrorCode.PRIVATE_CHAT_ROOM_INVALID_CONTEXT);
    }

    public static InvalidChatRoomContextException invalidGroupContext() {
        return new InvalidChatRoomContextException(ErrorCode.GROUP_CHAT_ROOM_INVALID_CONTEXT);
    }
}
