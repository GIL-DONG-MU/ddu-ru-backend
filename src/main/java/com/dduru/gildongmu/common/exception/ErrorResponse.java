package com.dduru.gildongmu.common.exception;

import lombok.Builder;

@Builder
public record ErrorResponse(
        int status,
        ErrorData data
) {

    public static ErrorResponse of(ErrorCode errorCode) {
        return ErrorResponse.builder()
                .status(errorCode.getStatus())
                .data(buildData(errorCode, null, errorCode.getMessage()))
                .build();
    }

    public static ErrorResponse of(ErrorCode errorCode, String message) {
        return ErrorResponse.builder()
                .status(errorCode.getStatus())
                .data(buildData(errorCode, null, resolveMessage(message, errorCode)))
                .build();
    }

    public static ErrorResponse ofField(ErrorCode code, String field, String message) {
        return ErrorResponse.builder()
                .status(code.getStatus())
                .data(buildData(code, field, resolveMessage(message, code)))
                .build();
    }

    private static ErrorData buildData(ErrorCode code, String field, String message) {
        return new ErrorData(code.name(), field, message);
    }

    private static String resolveMessage(String message, ErrorCode code) {
        if (message == null || message.isBlank()) {
            return code.getMessage();
        }
        return message;
    }
}
