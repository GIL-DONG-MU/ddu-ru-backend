package com.dduru.gildongmu.journey.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class InvalidJourneyScheduleException extends BusinessException {

    private InvalidJourneyScheduleException(ErrorCode errorCode) {
        super(errorCode);
    }

    public static InvalidJourneyScheduleException invalidTitle() {
        return new InvalidJourneyScheduleException(ErrorCode.JOURNEY_SCHEDULE_INVALID_TITLE);
    }

    public static InvalidJourneyScheduleException invalidMemo() {
        return new InvalidJourneyScheduleException(ErrorCode.JOURNEY_SCHEDULE_INVALID_MEMO);
    }

    public static InvalidJourneyScheduleException invalidPlaceName() {
        return new InvalidJourneyScheduleException(ErrorCode.JOURNEY_SCHEDULE_INVALID_PLACE_NAME);
    }

    public static InvalidJourneyScheduleException invalidScheduleTime() {
        return new InvalidJourneyScheduleException(ErrorCode.JOURNEY_SCHEDULE_INVALID_TIME);
    }

    public static InvalidJourneyScheduleException emptyPatch() {
        return new InvalidJourneyScheduleException(ErrorCode.JOURNEY_SCHEDULE_EMPTY_PATCH);
    }

    public static InvalidJourneyScheduleException invalidScheduleDate() {
        return new InvalidJourneyScheduleException(ErrorCode.JOURNEY_SCHEDULE_INVALID_DATE);
    }
}
