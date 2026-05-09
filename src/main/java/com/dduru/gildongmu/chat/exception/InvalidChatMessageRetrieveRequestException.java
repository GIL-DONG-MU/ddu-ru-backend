package com.dduru.gildongmu.chat.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class InvalidChatMessageRetrieveRequestException extends BusinessException {

    private InvalidChatMessageRetrieveRequestException(String message) {
        super(ErrorCode.INVALID_INPUT_VALUE, message);
    }

    public static InvalidChatMessageRetrieveRequestException fromMessage(String message) {
        return new InvalidChatMessageRetrieveRequestException(message);
    }
}
