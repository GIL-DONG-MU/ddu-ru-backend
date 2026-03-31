package com.dduru.gildongmu.chat.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class NotSelfChatException extends BusinessException {
    public NotSelfChatException() {
        super(ErrorCode.NOT_SELF_CHAT);
    }
}
