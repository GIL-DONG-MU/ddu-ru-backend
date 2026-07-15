package com.dduru.gildongmu.recommendation.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class InvalidAvailableDateException extends BusinessException {
    public InvalidAvailableDateException() {
        super(ErrorCode.INVALID_AVAILABLE_DATE);
    }
}
