package com.dduru.gildongmu.participation.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class RecruitmentClosedException extends BusinessException {
    public RecruitmentClosedException() {
        super(ErrorCode.RECRUITMENT_CLOSED);
    }

    public RecruitmentClosedException(String message) {
        super(ErrorCode.RECRUITMENT_CLOSED, message);
    }
}
