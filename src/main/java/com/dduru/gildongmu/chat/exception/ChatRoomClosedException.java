package com.dduru.gildongmu.chat.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class ChatRoomClosedException extends BusinessException {
    public ChatRoomClosedException() {
        super(ErrorCode.CHAT_ROOM_CLOSED);
    }
}
