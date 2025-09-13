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

    public static InvalidPostDateException endBeforeStart() {
        return new InvalidPostDateException("여행 종료일은 시작일과 같거나 이후여야 합니다");
    }

    public static InvalidPostDateException deadlineAfterStart() {
        return new InvalidPostDateException("모집 마감일은 여행 시작일과 같거나 이전이어야 합니다");
    }
}
