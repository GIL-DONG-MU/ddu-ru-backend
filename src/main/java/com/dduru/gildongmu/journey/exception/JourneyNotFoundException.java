package com.dduru.gildongmu.journey.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class JourneyNotFoundException extends BusinessException {

    public JourneyNotFoundException() {
        super(ErrorCode.JOURNEY_NOT_FOUND);
    }
}
