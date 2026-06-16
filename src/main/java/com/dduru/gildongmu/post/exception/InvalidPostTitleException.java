package com.dduru.gildongmu.post.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class InvalidPostTitleException extends BusinessException {
    public InvalidPostTitleException() {
        super(ErrorCode.INVALID_POST_TITLE);
    }
}
