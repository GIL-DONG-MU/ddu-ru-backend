package com.dduru.gildongmu.verification.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class InvalidVerificationCodeException extends BusinessException {
    public InvalidVerificationCodeException() {
        super(ErrorCode.INVALID_AUTH_CODE, "인증번호가 일치하지 않습니다.");
    }

    public InvalidVerificationCodeException(String message) {
        super(ErrorCode.INVALID_AUTH_CODE, message);
    }
}
