package com.dduru.gildongmu.auth.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class DuplicateEmailException extends BusinessException {
    public DuplicateEmailException() {
        super(ErrorCode.INVALID_INPUT_VALUE, "이미 다른 소셜 계정으로 가입된 이메일입니다.");
    }

    public DuplicateEmailException(String message) {
        super(ErrorCode.INVALID_INPUT_VALUE, message);
    }

    public static DuplicateEmailException of(String email) {
        return new DuplicateEmailException("이미 다른 소셜 계정으로 가입된 이메일입니다: " + email);
    }
}

