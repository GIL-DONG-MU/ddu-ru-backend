package com.dduru.gildongmu.journey.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class JourneyMemberNotFoundException extends BusinessException {

    public JourneyMemberNotFoundException() {
        super(ErrorCode.JOURNEY_MEMBER_NOT_FOUND);
    }
}
