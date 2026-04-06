package com.dduru.gildongmu.participation.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class RecruitmentClosedException extends BusinessException {
    private RecruitmentClosedException(ErrorCode errorCode) {
        super(errorCode);
    }

    public static RecruitmentClosedException isClosed() {
        return new RecruitmentClosedException(ErrorCode.RECRUITMENT_CLOSED);
    }

    public static RecruitmentClosedException isFulled() {
        return new RecruitmentClosedException(ErrorCode.RECRUITMENT_FULL);
    }
}
