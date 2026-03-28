package com.dduru.gildongmu.chat.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class UnauthorizedChatRoomCreationException extends BusinessException {
    public UnauthorizedChatRoomCreationException() {
        super(ErrorCode.UNAUTHORIZED_CHAT_ROOM_CREATION);
    }
}
