package com.dduru.gildongmu.post.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class InvalidRecruitCapacityException extends BusinessException {
    public InvalidRecruitCapacityException() {
        super(ErrorCode.INVALID_RECRUIT_CAPACITY);
    }
}
