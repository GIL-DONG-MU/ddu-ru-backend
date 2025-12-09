package com.dduru.gildongmu.common.exception;

public record ErrorData(
        String errorCode,
        String field,
        String reason
) {
}
