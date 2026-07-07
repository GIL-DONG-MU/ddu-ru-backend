package com.dduru.gildongmu.survey.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class InvalidTravelTendencyScoreException extends BusinessException {
    public InvalidTravelTendencyScoreException() {
        super(ErrorCode.INVALID_TRAVEL_TENDENCY_SCORE);
    }
}
