package com.dduru.gildongmu.profile.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class ProfileNotFoundException extends BusinessException {
    public ProfileNotFoundException() {
        super(ErrorCode.PROFILE_NOT_FOUND);
    }

    public ProfileNotFoundException(String message) {
        super(ErrorCode.PROFILE_NOT_FOUND, message);
    }

    public static ProfileNotFoundException of(Long userId) {
        return new ProfileNotFoundException("해당 유저의 프로필을 찾을 수 없습니다. userId=" + userId);
    }
}
