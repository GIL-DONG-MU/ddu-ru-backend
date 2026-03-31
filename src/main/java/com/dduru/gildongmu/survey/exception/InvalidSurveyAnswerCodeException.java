package com.dduru.gildongmu.survey.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class InvalidSurveyAnswerCodeException extends BusinessException {
    public InvalidSurveyAnswerCodeException() {
        super(ErrorCode.INVALID_INPUT_VALUE);
    }
}
