package com.dduru.gildongmu.profile.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class NicknameContainsBadWordException extends BusinessException {
    public NicknameContainsBadWordException() {
        super(ErrorCode.NICKNAME_CONTAINS_BAD_WORD);
    }
}
