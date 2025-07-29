package com.dduru.gildongmu.common.exception;

public class JsonConvertException extends BusinessException {
    public JsonConvertException() {
        super(ErrorCode.JSON_CONVERT_ERROR, "JSON 변환 중 오류가 발생했습니다");
    }

    public JsonConvertException(String message) {
        super(ErrorCode.JSON_CONVERT_ERROR, message);
    }
}
