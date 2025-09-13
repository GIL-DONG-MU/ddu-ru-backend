package com.dduru.gildongmu.post.exception;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;

public class DestinationNotFoundException extends BusinessException {
    public DestinationNotFoundException() {
        super(ErrorCode.DESTINATION_NOT_FOUND, "여행지를 찾을 수 없습니다");
    }

    public DestinationNotFoundException(String message) {
        super(ErrorCode.DESTINATION_NOT_FOUND, message);
    }

    public static DestinationNotFoundException of(Long destinationId) {
        return new DestinationNotFoundException("여행지를 찾을 수 없습니다. destinationId=" + destinationId);
    }
}
