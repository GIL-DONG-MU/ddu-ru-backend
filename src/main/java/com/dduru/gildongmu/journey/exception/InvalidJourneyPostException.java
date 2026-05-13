package com.dduru.gildongmu.journey.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class InvalidJourneyPostException extends BusinessException {

    private InvalidJourneyPostException(ErrorCode errorCode) {
        super(errorCode);
    }

    public static InvalidJourneyPostException emptyPatch() {
        return new InvalidJourneyPostException(ErrorCode.JOURNEY_POST_EMPTY_PATCH);
    }

    public static InvalidJourneyPostException invalidContent() {
        return new InvalidJourneyPostException(ErrorCode.JOURNEY_POST_INVALID_CONTENT);
    }
}
