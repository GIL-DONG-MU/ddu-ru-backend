package com.dduru.gildongmu.auth.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class RefreshTokenException extends BusinessException {
    public RefreshTokenException() {
        super(ErrorCode.INTERNAL_SERVER_ERROR, "Refresh token 처리 중 오류가 발생했습니다");
    }

    public RefreshTokenException(String message) {
        super(ErrorCode.INTERNAL_SERVER_ERROR, message);
    }
}
