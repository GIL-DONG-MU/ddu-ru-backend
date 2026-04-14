package com.dduru.gildongmu.superhost.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class SuperHostAlreadyActiveException extends BusinessException {
    public SuperHostAlreadyActiveException() {
        super(ErrorCode.SUPER_HOST_ALREADY_ACTIVE);
    }
}
