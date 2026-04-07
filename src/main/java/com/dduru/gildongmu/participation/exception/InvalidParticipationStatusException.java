package com.dduru.gildongmu.participation.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class InvalidParticipationStatusException extends BusinessException {
    public InvalidParticipationStatusException() {
        super(ErrorCode.INVALID_PARTICIPATION_STATUS);
    }
}
