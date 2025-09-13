package com.dduru.gildongmu.post.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class RecruitCountExceedCapacityException extends BusinessException {
    public RecruitCountExceedCapacityException() {
        super(ErrorCode.RECRUIT_COUNT_EXCEED_CAPACITY);
    }

    public RecruitCountExceedCapacityException(String message) {
        super(ErrorCode.RECRUIT_COUNT_EXCEED_CAPACITY, message);
    }
}
