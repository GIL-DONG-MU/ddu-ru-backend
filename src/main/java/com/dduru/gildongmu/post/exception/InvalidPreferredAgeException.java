package com.dduru.gildongmu.post.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class InvalidPreferredAgeException extends BusinessException {
    public InvalidPreferredAgeException(String message) {
        super(ErrorCode.INVALID_PREFERRED_AGE, message);
    }

    public static InvalidPreferredAgeException incompleteRange() {
        return new InvalidPreferredAgeException("선호 연령은 최소·최대를 함께 입력하거나, 연령 무관은 둘 다 비워 주세요");
    }

    public static InvalidPreferredAgeException conflictWithAgeAny() {
        return new InvalidPreferredAgeException("연령 무관일 때는 최소·최대 나이를 보낼 수 없습니다");
    }

    public static InvalidPreferredAgeException outOfBounds(int min, int max) {
        return new InvalidPreferredAgeException("선호 연령은 %d~%d 사이이며 최소값이 최대값을 넘을 수 없습니다".formatted(min, max));
    }
}
