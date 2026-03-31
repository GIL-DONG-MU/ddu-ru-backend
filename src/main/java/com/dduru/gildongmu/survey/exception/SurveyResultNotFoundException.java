package com.dduru.gildongmu.survey.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class SurveyResultNotFoundException extends BusinessException {
    public SurveyResultNotFoundException() {
        super(ErrorCode.SURVEY_RESULT_NOT_FOUND);
    }
}
