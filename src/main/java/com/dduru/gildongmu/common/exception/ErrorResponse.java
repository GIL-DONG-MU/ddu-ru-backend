package com.dduru.gildongmu.common.exception;

import lombok.Builder;

@Builder
public record ErrorResponse(
        int status,
        ErrorData data
) {

    public static ErrorResponse of(ErrorCode code) {
        return build(code, null, code.getMessage());
    }

    public static ErrorResponse of(ErrorCode code, String message) {
        return build(code, null, message);
    }

    public static ErrorResponse ofField(ErrorCode code, String field, String message) {
        return build(code, field, message);
    }

    private static ErrorResponse build(ErrorCode code, String field, String message) {
        return ErrorResponse.builder()
                .status(code.getStatus())
                .data(
                        new ErrorData(
                                code.name(),
                                field,
                                resolveReason(message, code)
                        ))
                .build();
    }

    private static String resolveReason(String reason, ErrorCode code) {
        if (reason == null || reason.isBlank()) {
            return code.getMessage();
        }
        return reason;
    }
}
