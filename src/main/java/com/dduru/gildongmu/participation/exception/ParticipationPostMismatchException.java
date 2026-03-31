package com.dduru.gildongmu.participation.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class ParticipationPostMismatchException extends BusinessException {
    public ParticipationPostMismatchException() {
        super(ErrorCode.PARTICIPATION_POST_MISMATCH);
    }
}
