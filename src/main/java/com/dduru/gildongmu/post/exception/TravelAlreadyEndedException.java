package com.dduru.gildongmu.post.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class TravelAlreadyEndedException extends BusinessException {
    public TravelAlreadyEndedException() {
        super(ErrorCode.TRAVEL_ALREADY_ENDED, "이미 종료된 여행입니다");
    }
    public TravelAlreadyEndedException(String message) {
        super(ErrorCode.TRAVEL_ALREADY_ENDED, message);
    }
}
