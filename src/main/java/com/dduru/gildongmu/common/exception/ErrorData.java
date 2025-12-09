package com.dduru.gildongmu.common.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorData(
        String errorCode,
        String field,
        String message
) {
}
