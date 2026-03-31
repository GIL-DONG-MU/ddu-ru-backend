package com.dduru.gildongmu.participation.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class SelfParticipationNotAllowedException extends BusinessException {
    
    public SelfParticipationNotAllowedException() {
        super(ErrorCode.SELF_PARTICIPATION_NOT_ALLOWED);
    }
}
