package com.dduru.gildongmu.post.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class InvalidRecruitSettingsException extends BusinessException {
    public InvalidRecruitSettingsException() {
        super(ErrorCode.INVALID_INPUT_VALUE, "공개 모집일 때는 동행 방식을 선택해야 합니다.");
    }

    public InvalidRecruitSettingsException(String message) {
        super(ErrorCode.INVALID_INPUT_VALUE, message);
    }
}
