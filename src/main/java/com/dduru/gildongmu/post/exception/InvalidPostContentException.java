package com.dduru.gildongmu.post.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class InvalidPostContentException extends BusinessException {
    public InvalidPostContentException() {
        super(ErrorCode.INVALID_POST_CONTENT);
    }
}
