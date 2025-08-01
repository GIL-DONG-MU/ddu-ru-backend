package com.dduru.gildongmu.post.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class PostNotFoundException extends BusinessException {
    public PostNotFoundException() {
        super(ErrorCode.POST_NOT_FOUND, "게시글을 찾을 수 없습니다");
    }

    public PostNotFoundException(String message) {
        super(ErrorCode.POST_NOT_FOUND, message);
    }
}
