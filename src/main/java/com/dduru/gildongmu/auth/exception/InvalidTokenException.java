package com.dduru.gildongmu.auth.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class InvalidTokenException extends BusinessException {
    public InvalidTokenException() {
        super(ErrorCode.INVALID_TOKEN, "유효하지 않은 토큰입니다");
    }

    public InvalidTokenException(String message) {
        super(ErrorCode.INVALID_TOKEN, message);
    }
}
