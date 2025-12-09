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
                                resolveMessage(message, code)
                        ))
                .build();
    }

    private static String resolveMessage(String message, ErrorCode code) {
        if (message == null || message.isBlank()) {
            return code.getMessage();
        }
        return message;
    }
}
