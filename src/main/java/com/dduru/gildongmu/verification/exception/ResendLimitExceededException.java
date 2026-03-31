package com.dduru.gildongmu.verification.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class ResendLimitExceededException extends BusinessException {
    public ResendLimitExceededException() {
        super(ErrorCode.TOO_MANY_REQUESTS);
    }
}
