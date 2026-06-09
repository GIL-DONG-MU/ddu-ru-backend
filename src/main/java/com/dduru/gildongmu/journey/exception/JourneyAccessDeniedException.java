package com.dduru.gildongmu.journey.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class JourneyAccessDeniedException extends BusinessException {

    public JourneyAccessDeniedException() {
        super(ErrorCode.JOURNEY_ACCESS_DENIED);
    }
}
