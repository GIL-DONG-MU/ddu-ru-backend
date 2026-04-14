package com.dduru.gildongmu.superhost.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class SuperHostTicketInvalidDurationException extends BusinessException {
    public SuperHostTicketInvalidDurationException() {
        super(ErrorCode.INVALID_INPUT_VALUE);
    }
}
