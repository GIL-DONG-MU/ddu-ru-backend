package com.dduru.gildongmu.journey.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class JourneyHostNotFoundException extends BusinessException {

    public JourneyHostNotFoundException() {
        super(ErrorCode.JOURNEY_HOST_NOT_FOUND);
    }
}
