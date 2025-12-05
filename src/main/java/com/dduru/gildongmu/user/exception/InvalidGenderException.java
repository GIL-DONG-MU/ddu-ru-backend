package com.dduru.gildongmu.user.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class InvalidGenderException extends BusinessException {
    public InvalidGenderException() {
        super(ErrorCode.INVALID_INPUT_VALUE, "잘못된 성별 값입니다.");
    }

    public InvalidGenderException(String message) {
        super(ErrorCode.INVALID_INPUT_VALUE, message);
    }

    public static InvalidGenderException invalidValue(String gender) {
        return new InvalidGenderException("잘못된 성별 값입니다: '" + gender + "'. 유효한 값: M, F, U");
    }
}

