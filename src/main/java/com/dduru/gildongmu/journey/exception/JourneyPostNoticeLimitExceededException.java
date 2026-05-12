package com.dduru.gildongmu.journey.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class JourneyPostNoticeLimitExceededException extends BusinessException {

    public JourneyPostNoticeLimitExceededException() {
        super(ErrorCode.JOURNEY_POST_NOTICE_LIMIT_EXCEEDED);
    }
}
