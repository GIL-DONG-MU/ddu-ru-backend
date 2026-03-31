package com.dduru.gildongmu.post.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class TravelAlreadyEndedException extends BusinessException {
    public TravelAlreadyEndedException() {
        super(ErrorCode.TRAVEL_ALREADY_ENDED);
    }
}
