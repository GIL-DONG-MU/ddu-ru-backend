package com.dduru.gildongmu.verification.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class VerificationNotFoundException extends BusinessException {
    public VerificationNotFoundException() {
        super(ErrorCode.VERIFICATION_NOT_FOUND, "인증 정보를 찾을 수 없습니다.");
    }

    public VerificationNotFoundException(String message) {
        super(ErrorCode.VERIFICATION_NOT_FOUND, message);
    }
}
