package com.dduru.gildongmu.profile.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class BgColorNotFoundException extends BusinessException {
    public BgColorNotFoundException() {
        super(ErrorCode.BG_COLOR_NOT_FOUND, "배경색을 찾을 수 없습니다");
    }

    public BgColorNotFoundException(String message) {
        super(ErrorCode.BG_COLOR_NOT_FOUND, message);
    }

    public static BgColorNotFoundException of(Long bgColorId) {
        return new BgColorNotFoundException("배경색을 찾을 수 없습니다. bgColorId=" + bgColorId);
    }
}
