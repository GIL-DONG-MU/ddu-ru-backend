package com.dduru.gildongmu.verification.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class AlreadyVerifiedException extends BusinessException {
    public AlreadyVerifiedException() {
        super(ErrorCode.ALREADY_VERIFIED, "이미 완료된 인증입니다.");
    }

    public AlreadyVerifiedException(String message) {
        super(ErrorCode.ALREADY_VERIFIED, message);
    }
}
