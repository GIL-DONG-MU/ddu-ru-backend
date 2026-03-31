package com.dduru.gildongmu.common.exception;

public class JsonConvertException extends BusinessException {
    public JsonConvertException() {
        super(ErrorCode.JSON_CONVERT_ERROR);
    }
}
