package com.dduru.gildongmu.superhost.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class SuperHostPostNotApplicableException extends BusinessException {
    public SuperHostPostNotApplicableException() {
        super(ErrorCode.SUPER_HOST_POST_NOT_APPLICABLE);
    }
}
