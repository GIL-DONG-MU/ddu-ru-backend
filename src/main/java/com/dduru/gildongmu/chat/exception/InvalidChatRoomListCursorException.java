package com.dduru.gildongmu.chat.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class InvalidChatRoomListCursorException extends BusinessException {

    private static final String MESSAGE = "cursor 형식이 올바르지 않습니다.";

    public InvalidChatRoomListCursorException() {
        super(ErrorCode.INVALID_INPUT_VALUE, MESSAGE);
    }
}
