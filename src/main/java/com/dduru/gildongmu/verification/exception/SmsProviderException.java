package com.dduru.gildongmu.verification.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class SmsProviderException extends BusinessException {
    public SmsProviderException() {
        super(ErrorCode.SMS_PROVIDER_ERROR);
    }
}
