package com.dduru.gildongmu.journey.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class InvalidJourneyBasicInfoException extends BusinessException {

    private InvalidJourneyBasicInfoException(ErrorCode errorCode) {
        super(errorCode);
    }

    public static InvalidJourneyBasicInfoException emptyPatch() {
        return new InvalidJourneyBasicInfoException(ErrorCode.JOURNEY_EMPTY_PATCH);
    }

    public static InvalidJourneyBasicInfoException invalidTitleLength() {
        return new InvalidJourneyBasicInfoException(ErrorCode.JOURNEY_INVALID_TITLE_LENGTH);
    }

    public static InvalidJourneyBasicInfoException invalidPhotoUrl() {
        return new InvalidJourneyBasicInfoException(ErrorCode.JOURNEY_INVALID_PHOTO_URL);
    }
}
