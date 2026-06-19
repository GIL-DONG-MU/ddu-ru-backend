package com.dduru.gildongmu.journey.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class JourneyMemberRemovalException extends BusinessException {

    private JourneyMemberRemovalException(ErrorCode errorCode) {
        super(errorCode);
    }

    public static JourneyMemberRemovalException cannotRemoveSelf() {
        return new JourneyMemberRemovalException(ErrorCode.JOURNEY_MEMBER_CANNOT_REMOVE_SELF);
    }
}
