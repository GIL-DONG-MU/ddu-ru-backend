package com.dduru.gildongmu.survey.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class SurveyRetakeLockedException extends BusinessException {
    public SurveyRetakeLockedException() {
        super(ErrorCode.SURVEY_RETAKE_LOCKED);
    }
}
