package com.dduru.gildongmu.profile.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class NicknameNotBlankException extends BusinessException {
    public NicknameNotBlankException() {
        super(ErrorCode.NICKNAME_NOT_BLANK);
    }
}
