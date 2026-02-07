package com.dduru.gildongmu.post.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;
import com.dduru.gildongmu.post.domain.enums.PostStatus;

public class InvalidPostStatusException extends BusinessException {
    public InvalidPostStatusException() {
        super(ErrorCode.INVALID_POST_STATUS, "모집이 완료된 게시글은 모집 상태를 변경할 수 없습니다.");
    }

    public InvalidPostStatusException(String message) {
        super(ErrorCode.INVALID_POST_STATUS, message);
    }

    public static InvalidPostStatusException cannotTransition(PostStatus current, PostStatus requested) {
        return new InvalidPostStatusException(
                "모집이 완료된 게시글은 모집 상태를 변경할 수 없습니다. (현재: " + current + ", 요청: " + requested + ")");
    }
}
