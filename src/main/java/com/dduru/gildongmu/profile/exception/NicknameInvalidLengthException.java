package com.dduru.gildongmu.profile.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class NicknameInvalidLengthException extends BusinessException {
    public NicknameInvalidLengthException() {
        super(ErrorCode.NICKNAME_INVALID_LENGTH);
    }
}
