package com.dduru.gildongmu.chat.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class ChatAccessDeniedException extends BusinessException {
    public ChatAccessDeniedException() {
        super(ErrorCode.CHAT_ACCESS_DENIED);
    }
}
