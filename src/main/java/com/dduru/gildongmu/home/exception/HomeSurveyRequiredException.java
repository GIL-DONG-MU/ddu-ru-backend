package com.dduru.gildongmu.home.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class HomeSurveyRequiredException extends BusinessException {

    public HomeSurveyRequiredException() {
        super(ErrorCode.SURVEY_REQUIRED);
    }
}
