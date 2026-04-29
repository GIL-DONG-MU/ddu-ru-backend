package com.dduru.gildongmu.chat.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class InvalidChatImageUrlException extends BusinessException {

    private InvalidChatImageUrlException(ErrorCode errorCode) {
        super(errorCode);
    }

    public static InvalidChatImageUrlException missing() {
        return new InvalidChatImageUrlException(ErrorCode.CHAT_IMAGE_URL_REQUIRED);
    }

    public static InvalidChatImageUrlException blank() {
        return new InvalidChatImageUrlException(ErrorCode.CHAT_IMAGE_URL_BLANK);
    }

    public static InvalidChatImageUrlException tooLong() {
        return new InvalidChatImageUrlException(ErrorCode.CHAT_IMAGE_URL_TOO_LONG);
    }

    public static InvalidChatImageUrlException invalidFormat() {
        return new InvalidChatImageUrlException(ErrorCode.CHAT_IMAGE_URL_INVALID_FORMAT);
    }

    public static InvalidChatImageUrlException notAbsolute() {
        return new InvalidChatImageUrlException(ErrorCode.CHAT_IMAGE_URL_NOT_ABSOLUTE);
    }

    public static InvalidChatImageUrlException nonHttpsScheme() {
        return new InvalidChatImageUrlException(ErrorCode.CHAT_IMAGE_URL_INVALID_SCHEME);
    }

    public static InvalidChatImageUrlException notAllowed() {
        return new InvalidChatImageUrlException(ErrorCode.CHAT_IMAGE_URL_NOT_ALLOWED);
    }
}
