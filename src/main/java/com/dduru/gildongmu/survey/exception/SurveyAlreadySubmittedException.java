package com.dduru.gildongmu.survey.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class SurveyAlreadySubmittedException extends BusinessException {
    public SurveyAlreadySubmittedException() {
        super(ErrorCode.SURVEY_ALREADY_SUBMITTED);
    }
}
