package com.dduru.gildongmu.profile.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class InvalidProfileImageUrlException extends BusinessException {
    public InvalidProfileImageUrlException() {
        super(ErrorCode.INVALID_PROFILE_IMAGE_URL);
    }
}
