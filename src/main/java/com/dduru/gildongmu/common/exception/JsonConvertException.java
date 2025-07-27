package com.dduru.gildongmu.common.exception;

public class JsonConvertException extends BusinessException{
    public JsonConvertException() {
        super(ErrorCode.JSON_CONVERT_ERROR);
    }

    public JsonConvertException(String message, Throwable cause) {
        super(ErrorCode.JSON_CONVERT_ERROR, message);
        initCause(cause);
    }
}
