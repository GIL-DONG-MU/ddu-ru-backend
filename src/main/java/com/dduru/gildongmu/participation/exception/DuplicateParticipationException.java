package com.dduru.gildongmu.participation.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class DuplicateParticipationException extends BusinessException {
    public DuplicateParticipationException() {
        super(ErrorCode.DUPLICATE_PARTICIPATION);
    }

    public DuplicateParticipationException(String message) {
        super(ErrorCode.DUPLICATE_PARTICIPATION, message);
    }
}
