package com.dduru.gildongmu.verification.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class SmsProviderException extends BusinessException {
    public SmsProviderException() {
        super(ErrorCode.SMS_PROVIDER_ERROR, "SMS 발송 서비스에 일시적인 오류가 발생했습니다.");
    }

    public SmsProviderException(String message) {
        super(ErrorCode.SMS_PROVIDER_ERROR, message);
    }
}
