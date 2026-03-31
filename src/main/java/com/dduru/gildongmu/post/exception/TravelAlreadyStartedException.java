package com.dduru.gildongmu.post.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class TravelAlreadyStartedException extends BusinessException {
    public TravelAlreadyStartedException() {
        super(ErrorCode.TRAVEL_ALREADY_STARTED);
    }
}
