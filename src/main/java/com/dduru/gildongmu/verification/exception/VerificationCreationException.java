package com.dduru.gildongmu.verification.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class VerificationCreationException extends BusinessException {
    public VerificationCreationException() {
        super(ErrorCode.INTERNAL_SERVER_ERROR, "인증 정보 생성에 실패했습니다.");
    }

    public VerificationCreationException(String message) {
        super(ErrorCode.INTERNAL_SERVER_ERROR, message);
    }
}
