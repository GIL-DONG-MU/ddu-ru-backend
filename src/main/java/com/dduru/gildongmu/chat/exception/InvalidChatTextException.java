package com.dduru.gildongmu.chat.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class InvalidChatTextException extends BusinessException {

    private InvalidChatTextException(ErrorCode errorCode) {
        super(errorCode);
    }

    public static InvalidChatTextException missing() {
        return new InvalidChatTextException(ErrorCode.CHAT_TEXT_REQUIRED);
    }

    public static InvalidChatTextException blank() {
        return new InvalidChatTextException(ErrorCode.CHAT_TEXT_BLANK);
    }

    public static InvalidChatTextException tooLong() {
        return new InvalidChatTextException(ErrorCode.CHAT_TEXT_TOO_LONG);
    }
}
