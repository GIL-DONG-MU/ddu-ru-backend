package com.dduru.gildongmu.auth.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class UserNotFoundException extends BusinessException {
    public UserNotFoundException() {
        super(ErrorCode.USER_NOT_FOUND, "사용자를 찾을 수 없습니다");
    }

    public UserNotFoundException(String message) {
        super(ErrorCode.USER_NOT_FOUND, message);
    }

    public static UserNotFoundException of(Long userId) {
        return new UserNotFoundException("사용자를 찾을 수 없습니다. userId=" + userId);
    }
}
