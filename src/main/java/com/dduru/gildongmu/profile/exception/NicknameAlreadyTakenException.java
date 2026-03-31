package com.dduru.gildongmu.profile.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class NicknameAlreadyTakenException extends BusinessException {
    public NicknameAlreadyTakenException() {
        super(ErrorCode.NICKNAME_ALREADY_TAKEN);
    }
}
