package com.dduru.gildongmu.post.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class InvalidRecruitCapacityException extends BusinessException {
    public InvalidRecruitCapacityException() {
        super(ErrorCode.INVALID_RECRUIT_CAPACITY, "잘못된 모집 인원 설정입니다");
    }

    public InvalidRecruitCapacityException(String message) {
        super(ErrorCode.INVALID_RECRUIT_CAPACITY, message);
    }

    public static InvalidRecruitCapacityException insufficientCapacity(int current, int requested) {
        return new InvalidRecruitCapacityException(
                String.format("모집 인원은 현재 신청자 수(%d명)보다 적을 수 없습니다. 요청된 인원: %d명", current, requested)
        );
    }
}
