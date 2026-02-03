package com.dduru.gildongmu.profile.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class InvalidProfileImageUrlException extends BusinessException {
    public InvalidProfileImageUrlException() {
        super(ErrorCode.INVALID_PROFILE_IMAGE_URL);
    }

    public InvalidProfileImageUrlException(String message) {
        super(ErrorCode.INVALID_PROFILE_IMAGE_URL, message);
    }

    public static InvalidProfileImageUrlException uploadedTypeRequiresUrl() {
        return new InvalidProfileImageUrlException("UPLOADED 타입일 때는 업로드된 이미지 URL이 필요합니다.");
    }
}
