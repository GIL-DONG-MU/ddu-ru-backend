package com.dduru.gildongmu.post.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class InvalidPostDateException extends BusinessException {
    public InvalidPostDateException() {
        super(ErrorCode.INVALID_POST_DATE, "잘못된 날짜 설정입니다");
    }

    public InvalidPostDateException(String message) {
        super(ErrorCode.INVALID_POST_DATE, message);
    }
}
