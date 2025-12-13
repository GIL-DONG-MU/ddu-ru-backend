package com.dduru.gildongmu.verification.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class ResendLimitExceededException extends BusinessException {
    public ResendLimitExceededException() {
        super(ErrorCode.TOO_MANY_REQUESTS, "재발송 제한 시간이 지나지 않았습니다.");
    }

    public ResendLimitExceededException(String message) {
        super(ErrorCode.TOO_MANY_REQUESTS, message);
    }
}
