package com.dduru.gildongmu.destination.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class DestinationNotFoundException extends BusinessException {
    public DestinationNotFoundException() {
        super(ErrorCode.DESTINATION_NOT_FOUND);
    }
}
