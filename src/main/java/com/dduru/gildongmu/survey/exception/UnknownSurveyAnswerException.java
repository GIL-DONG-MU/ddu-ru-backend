package com.dduru.gildongmu.survey.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class UnknownSurveyAnswerException extends BusinessException {
    public UnknownSurveyAnswerException(Integer code) {
        super(ErrorCode.UNKNOWN_SURVEY_ANSWER_CODE, "알 수 없는 설문조사 답변 코드입니다: " + code);
    }
}
