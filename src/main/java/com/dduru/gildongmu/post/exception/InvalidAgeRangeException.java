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
    public static InvalidAgeRangeException maxLessThanMin() {
        return new InvalidAgeRangeException("최대 연령은 최소 연령보다 크거나 같아야 합니다");
    }
}
