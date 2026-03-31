package com.dduru.gildongmu.onboarding.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class SurveyAlreadyCompletedException extends BusinessException {
    public SurveyAlreadyCompletedException() {
        super(ErrorCode.SURVEY_ALREADY_COMPLETED);
    }
}
