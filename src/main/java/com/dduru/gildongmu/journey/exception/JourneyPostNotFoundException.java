package com.dduru.gildongmu.journey.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class JourneyPostNotFoundException extends BusinessException {

    public JourneyPostNotFoundException() {
        super(ErrorCode.JOURNEY_POST_NOT_FOUND);
    }
}
