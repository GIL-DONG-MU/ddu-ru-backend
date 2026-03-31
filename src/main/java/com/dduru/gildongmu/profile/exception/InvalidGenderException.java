package com.dduru.gildongmu.profile.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class InvalidGenderException extends BusinessException {
    public InvalidGenderException() {
        super(ErrorCode.INVALID_INPUT_VALUE);
    }
}

