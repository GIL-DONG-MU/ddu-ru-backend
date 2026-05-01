package com.dduru.gildongmu.journey.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class InvalidJourneyBasicInfoException extends BusinessException {
    private InvalidJourneyBasicInfoException(String message) {
        super(ErrorCode.INVALID_INPUT_VALUE, message);
    }

    public static InvalidJourneyBasicInfoException emptyPatch() {
        return new InvalidJourneyBasicInfoException("제목 또는 대표 사진 중 하나는 입력해야 합니다.");
    }

    public static InvalidJourneyBasicInfoException invalidTitleLength() {
        return new InvalidJourneyBasicInfoException("제목은 5자 이상 40자 이하여야 합니다.");
    }
}
