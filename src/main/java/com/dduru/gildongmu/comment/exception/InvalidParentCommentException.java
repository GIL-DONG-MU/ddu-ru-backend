package com.dduru.gildongmu.comment.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class InvalidParentCommentException extends BusinessException {
    public InvalidParentCommentException() {
        super(ErrorCode.INVALID_PARENT_COMMENT);
    }
}
