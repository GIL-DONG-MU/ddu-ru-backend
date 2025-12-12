package com.dduru.gildongmu.auth.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class DuplicateEmailException extends BusinessException {
    public DuplicateEmailException() {
        super(ErrorCode.DUPLICATE_EMAIL, "이미 다른 소셜 계정으로 가입된 이메일입니다.");
    }

    public DuplicateEmailException(String message) {
        super(ErrorCode.DUPLICATE_EMAIL, message);
    }

    public static DuplicateEmailException of(String email) {
        return new DuplicateEmailException("이미 다른 소셜 계정으로 가입된 이메일입니다: " + email);
    }
}

