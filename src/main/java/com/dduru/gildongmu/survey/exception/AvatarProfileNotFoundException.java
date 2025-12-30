package com.dduru.gildongmu.survey.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;
import com.dduru.gildongmu.survey.domain.enums.AvatarType;

public class AvatarProfileNotFoundException extends BusinessException {
    public AvatarProfileNotFoundException() {
        super(ErrorCode.AVATAR_PROFILE_NOT_FOUND, "아바타 프로필을 찾을 수 없습니다");
    }

    public AvatarProfileNotFoundException(String message) {
        super(ErrorCode.AVATAR_PROFILE_NOT_FOUND, message);
    }

    public static AvatarProfileNotFoundException of(AvatarType avatarType) {
        return new AvatarProfileNotFoundException("아바타 프로필을 찾을 수 없습니다. avatarType=" + avatarType);
    }
}
