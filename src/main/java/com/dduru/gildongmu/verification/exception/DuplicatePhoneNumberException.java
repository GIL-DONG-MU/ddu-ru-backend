package com.dduru.gildongmu.verification.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class DuplicatePhoneNumberException extends BusinessException {
    public DuplicatePhoneNumberException() {
        super(ErrorCode.DUPLICATE_PHONE_NUMBER, "이미 가입된 전화번호입니다.");
    }

    public DuplicatePhoneNumberException(String message) {
        super(ErrorCode.DUPLICATE_PHONE_NUMBER, message);
    }
}
