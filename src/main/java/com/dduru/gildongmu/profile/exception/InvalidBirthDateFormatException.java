package com.dduru.gildongmu.profile.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class InvalidBirthDateFormatException extends BusinessException {
    public InvalidBirthDateFormatException(String message) {
        super(ErrorCode.INVALID_INPUT_VALUE, message);
    }

    public static InvalidBirthDateFormatException invalidFormat() {
        return new InvalidBirthDateFormatException("생년월일 형식이 올바르지 않습니다. (yyyy-MM-dd 형식)");
    }
}
