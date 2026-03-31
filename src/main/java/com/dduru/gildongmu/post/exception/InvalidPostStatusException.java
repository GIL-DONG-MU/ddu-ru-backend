package com.dduru.gildongmu.post.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class InvalidPostStatusException extends BusinessException {
    public InvalidPostStatusException() {
        super(ErrorCode.INVALID_POST_STATUS);
    }
}
