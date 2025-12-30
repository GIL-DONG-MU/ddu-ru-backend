package com.dduru.gildongmu.survey.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class InvalidSurveyAnswerCodeException extends BusinessException {
    public InvalidSurveyAnswerCodeException() {
        super(ErrorCode.INVALID_INPUT_VALUE, "유효하지 않은 설문 답변 코드입니다");
    }

    public InvalidSurveyAnswerCodeException(String message) {
        super(ErrorCode.INVALID_INPUT_VALUE, message);
    }

    public static InvalidSurveyAnswerCodeException of(String question, Integer code) {
        return new InvalidSurveyAnswerCodeException(question + "에 대한 유효하지 않은 답변 코드입니다: " + code);
    }
}
