package com.dduru.gildongmu.journey.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class CurrentOrUpcomingJourneyNotFoundException extends BusinessException {

    public CurrentOrUpcomingJourneyNotFoundException() {
        super(ErrorCode.CURRENT_OR_UPCOMING_JOURNEY_NOT_FOUND);
    }
}
