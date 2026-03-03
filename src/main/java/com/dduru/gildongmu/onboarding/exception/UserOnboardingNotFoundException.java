package com.dduru.gildongmu.onboarding.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class UserOnboardingNotFoundException extends BusinessException {
    private UserOnboardingNotFoundException(String message) {
        super(ErrorCode.USER_ONBOARDING_NOT_FOUND, message);
    }

    public static UserOnboardingNotFoundException of(Long userId) {
        return new UserOnboardingNotFoundException("userId= " + userId);
    }
}
