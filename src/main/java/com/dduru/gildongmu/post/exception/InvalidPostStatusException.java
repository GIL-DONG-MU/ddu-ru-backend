package com.dduru.gildongmu.post.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class InvalidPostStatusException extends BusinessException {
    public InvalidPostStatusException() {
        super(ErrorCode.INVALID_POST_STATUS, "모집이 완료된 게시글은 모집 상태를 변경할 수 없습니다.");
    }
    public InvalidPostStatusException(String message) {
        super(ErrorCode.INVALID_POST_STATUS, message);
    }
}
