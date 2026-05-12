package com.dduru.gildongmu.journey.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class JourneyPostAccessDeniedException extends BusinessException {

    public JourneyPostAccessDeniedException() {
        super(ErrorCode.JOURNEY_POST_ACCESS_DENIED);
    }
}
