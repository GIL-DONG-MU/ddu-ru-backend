package com.dduru.gildongmu.chat.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class ChatMessageNotFoundException extends BusinessException {
    public ChatMessageNotFoundException() {
        super(ErrorCode.CHAT_MESSAGE_NOT_FOUND);
    }
}
