package com.dduru.gildongmu.profile.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class NicknameInvalidCharactersException extends BusinessException {
    public NicknameInvalidCharactersException() {
        super(ErrorCode.NICKNAME_INVALID_CHARACTERS);
    }
}
