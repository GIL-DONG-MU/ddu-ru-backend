package com.dduru.gildongmu.auth.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class UnsupportedOauthTypeException extends BusinessException {
    public UnsupportedOauthTypeException() {
        super(ErrorCode.UNSUPPORTED_SOCIAL_LOGIN, "지원하지 않는 소셜 로그인입니다.");
    }

    public UnsupportedOauthTypeException(String message) {
        super(ErrorCode.UNSUPPORTED_SOCIAL_LOGIN, message);
    }

    public static UnsupportedOauthTypeException of(String oauthType) {
        return new UnsupportedOauthTypeException("지원하지 않는 소셜 로그인 타입입니다: " + oauthType);
    }
}

