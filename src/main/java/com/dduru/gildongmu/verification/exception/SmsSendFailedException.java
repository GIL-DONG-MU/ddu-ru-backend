package com.dduru.gildongmu.verification.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class SmsSendFailedException extends BusinessException {
    public SmsSendFailedException() {
        super(ErrorCode.SMS_SEND_FAILED);
    }
}
