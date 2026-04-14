package com.dduru.gildongmu.participation.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class ParticipationApplicantAccessDeniedException extends BusinessException {
    public ParticipationApplicantAccessDeniedException() {
        super(ErrorCode.ACCESS_DENIED);
    }
}
