package com.dduru.gildongmu.post.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class DestinationNotFoundException extends BusinessException {
    public DestinationNotFoundException() {
        super(ErrorCode.DESTINATION_NOT_FOUND);
    }

    public DestinationNotFoundException(Long destinationId) {
        super(ErrorCode.DESTINATION_NOT_FOUND, "여행지를 찾을 수 없습니다. destinationId: " + destinationId);
    }
}
