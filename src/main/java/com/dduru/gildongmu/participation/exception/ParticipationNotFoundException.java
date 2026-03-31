package com.dduru.gildongmu.participation.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class ParticipationNotFoundException extends BusinessException {
    public ParticipationNotFoundException() {
        super(ErrorCode.PARTICIPATION_NOT_FOUND);
    }
}
