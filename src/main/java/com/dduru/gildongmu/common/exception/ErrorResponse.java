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
                .data(buildData(errorCode.name(), null, errorCode.getMessage()))
                .build();
    }

    public static ErrorResponse of(ErrorCode errorCode, String reason) {
        return ErrorResponse.builder()
                .status(errorCode.getStatus())
                .data(buildData(errorCode.name(), null, resolveReason(reason, errorCode)))
                .build();
    }

    public static ErrorResponse ofField(ErrorCode errorCode, String field, String reason) {
        return ErrorResponse.builder()
                .status(errorCode.getStatus())
                .data(buildData(errorCode.name(), field, resolveReason(reason, errorCode)))
                .build();
    }

    private static ErrorData buildData(String errorCode, String field, String reason) {
        return new ErrorData(errorCode, field, reason);
    }

    private static String resolveReason(String reason, ErrorCode code) {
        if (reason == null || reason.isBlank()) {
            return code.getMessage();
        }
        return reason;
    }
}
