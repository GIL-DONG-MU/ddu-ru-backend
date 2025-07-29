package com.dduru.gildongmu.post.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class InvalidAgeRangeException extends BusinessException {
    public InvalidAgeRangeException() {
        super(ErrorCode.INVALID_AGE_RANGE, "연령대 설정이 올바르지 않습니다");
    }

    public InvalidAgeRangeException(String message) {
        super(ErrorCode.INVALID_AGE_RANGE, message);
    }
}
