package com.dduru.gildongmu.post.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class InvalidBudgetRangeException extends BusinessException {
    public InvalidBudgetRangeException() {
        super(ErrorCode.INVALID_BUDGET_RANGE, "최대 예산은 최소 예산보다 커야 합니다");
    }

    public InvalidBudgetRangeException(String message) {
        super(ErrorCode.INVALID_BUDGET_RANGE, message);
    }
}
