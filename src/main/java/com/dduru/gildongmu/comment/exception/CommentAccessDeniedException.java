package com.dduru.gildongmu.comment.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class CommentAccessDeniedException extends BusinessException {
    public CommentAccessDeniedException() {
        super(ErrorCode.COMMENT_ACCESS_DENIED);
    }
}
