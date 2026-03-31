package com.dduru.gildongmu.post.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class PostAccessDeniedException extends BusinessException {
    public PostAccessDeniedException() {
        super(ErrorCode.POST_ACCESS_DENIED);
    }
}
