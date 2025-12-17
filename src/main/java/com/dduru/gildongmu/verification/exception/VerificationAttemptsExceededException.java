package com.dduru.gildongmu.verification.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class VerificationAttemptsExceededException extends BusinessException {
    public VerificationAttemptsExceededException() {
        super(ErrorCode.VERIFICATION_ATTEMPTS_EXCEEDED, "검증 시도 횟수를 초과했습니다.");
    }

    public VerificationAttemptsExceededException(String message) {
        super(ErrorCode.VERIFICATION_ATTEMPTS_EXCEEDED, message);
    }
}
