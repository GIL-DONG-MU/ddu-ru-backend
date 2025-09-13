package com.dduru.gildongmu.auth.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class TokenRefreshFailedException extends BusinessException {
    public TokenRefreshFailedException() {
        super(ErrorCode.INTERNAL_SERVER_ERROR, "토큰 재발급에 실패했습니다");
    }

    public TokenRefreshFailedException(String message) {
        super(ErrorCode.INTERNAL_SERVER_ERROR, message);
    }
}
