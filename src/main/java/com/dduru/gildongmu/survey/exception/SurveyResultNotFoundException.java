package com.dduru.gildongmu.survey.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class SurveyResultNotFoundException extends BusinessException {
    public SurveyResultNotFoundException() {
        super(ErrorCode.SURVEY_RESULT_NOT_FOUND, "설문 결과를 찾을 수 없습니다");
    }

    public SurveyResultNotFoundException(String message) {
        super(ErrorCode.SURVEY_RESULT_NOT_FOUND, message);
    }

    public static SurveyResultNotFoundException of() {
        return new SurveyResultNotFoundException();
    }
}
