package com.dduru.gildongmu.auth.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class UnsupportedOauthTypeException extends BusinessException {
    public UnsupportedOauthTypeException() {
        super(ErrorCode.UNSUPPORTED_SOCIAL_LOGIN);
    }
}
