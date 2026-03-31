package com.dduru.gildongmu.auth.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class UserCreationIntegrityException extends BusinessException {
    public UserCreationIntegrityException() {
        super(ErrorCode.USER_CREATION_INTEGRITY_ERROR);
    }
}
