package com.dduru.gildongmu.common.validation;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class InvalidImageUrlException extends BusinessException {

    private InvalidImageUrlException(ErrorCode errorCode) {
        super(errorCode);
    }

    public static InvalidImageUrlException missing() {
        return new InvalidImageUrlException(ErrorCode.IMAGE_URL_REQUIRED);
    }

    public static InvalidImageUrlException blank() {
        return new InvalidImageUrlException(ErrorCode.IMAGE_URL_BLANK);
    }

    public static InvalidImageUrlException tooLong() {
        return new InvalidImageUrlException(ErrorCode.IMAGE_URL_TOO_LONG);
    }

    public static InvalidImageUrlException invalidFormat() {
        return new InvalidImageUrlException(ErrorCode.IMAGE_URL_INVALID_FORMAT);
    }

    public static InvalidImageUrlException notAbsolute() {
        return new InvalidImageUrlException(ErrorCode.IMAGE_URL_NOT_ABSOLUTE);
    }

    public static InvalidImageUrlException nonHttpsScheme() {
        return new InvalidImageUrlException(ErrorCode.IMAGE_URL_INVALID_SCHEME);
    }

    public static InvalidImageUrlException notAllowed() {
        return new InvalidImageUrlException(ErrorCode.IMAGE_URL_NOT_ALLOWED);
    }
}
