package com.dduru.gildongmu.verification.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class DailySmsLimitExceededException extends BusinessException {
    public DailySmsLimitExceededException() {
        super(ErrorCode.DAILY_SMS_LIMIT_EXCEEDED, "일일 발송 한도를 초과했습니다.");
    }

    public DailySmsLimitExceededException(String message) {
        super(ErrorCode.DAILY_SMS_LIMIT_EXCEEDED, message);
    }
}
