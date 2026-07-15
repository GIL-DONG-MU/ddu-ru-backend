package com.dduru.gildongmu.recommendation.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class InvalidDestinationPreferenceException extends BusinessException {
    public InvalidDestinationPreferenceException() {
        super(ErrorCode.INVALID_DESTINATION_PREFERENCE);
    }
}
