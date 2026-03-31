package com.dduru.gildongmu.common.exception;

public class InternalServerException extends BusinessException {
    public InternalServerException() {
        super(ErrorCode.INTERNAL_SERVER_ERROR);
    }
}
