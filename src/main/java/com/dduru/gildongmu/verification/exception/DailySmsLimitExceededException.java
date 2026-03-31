package com.dduru.gildongmu.verification.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class DailySmsLimitExceededException extends BusinessException {
    public DailySmsLimitExceededException() {
        super(ErrorCode.DAILY_SMS_LIMIT_EXCEEDED);
    }
}
