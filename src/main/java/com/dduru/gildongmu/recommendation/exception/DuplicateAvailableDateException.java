package com.dduru.gildongmu.recommendation.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class DuplicateAvailableDateException extends BusinessException {
    public DuplicateAvailableDateException() {
        super(ErrorCode.DUPLICATE_AVAILABLE_DATE);
    }
}
