package com.dduru.gildongmu.verification.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class SmsSendFailedException extends BusinessException {
    public SmsSendFailedException() {
        super(ErrorCode.SMS_SEND_FAILED, "SMS 발송에 실패했습니다.");
    }

    public SmsSendFailedException(String message) {
        super(ErrorCode.SMS_SEND_FAILED, message);
    }
}
