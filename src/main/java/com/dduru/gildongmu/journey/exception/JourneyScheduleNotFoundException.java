package com.dduru.gildongmu.journey.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class JourneyScheduleNotFoundException extends BusinessException {

    public JourneyScheduleNotFoundException() {
        super(ErrorCode.JOURNEY_SCHEDULE_NOT_FOUND);
    }
}
