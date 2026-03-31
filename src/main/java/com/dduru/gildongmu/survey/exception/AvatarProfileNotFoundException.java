package com.dduru.gildongmu.survey.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class AvatarProfileNotFoundException extends BusinessException {
    public AvatarProfileNotFoundException() {
        super(ErrorCode.AVATAR_PROFILE_NOT_FOUND);
    }
}
