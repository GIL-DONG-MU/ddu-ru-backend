package com.dduru.gildongmu.profile.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class BgColorNotFoundException extends BusinessException {
    public BgColorNotFoundException() {
        super(ErrorCode.BG_COLOR_NOT_FOUND);
    }
}
