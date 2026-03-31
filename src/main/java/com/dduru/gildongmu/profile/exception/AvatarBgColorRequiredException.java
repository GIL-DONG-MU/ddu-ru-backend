package com.dduru.gildongmu.profile.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class AvatarBgColorRequiredException extends BusinessException {
    public AvatarBgColorRequiredException() {
        super(ErrorCode.INVALID_INPUT_VALUE, "AVATAR 타입에는 bgColorId가 필요합니다.");
    }
}
