package com.dduru.gildongmu.post.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class RecruitCountBelowZeroException extends BusinessException {
    public RecruitCountBelowZeroException() {
        super(ErrorCode.RECRUIT_COUNT_BELOW_ZERO);
    }
}
