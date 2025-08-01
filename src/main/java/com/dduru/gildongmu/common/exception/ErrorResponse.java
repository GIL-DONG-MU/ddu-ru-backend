package com.dduru.gildongmu.common.exception;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ErrorResponse {
    private final String code;
    private final String message;
    private final LocalDateTime timestamp;

    @Builder
    public ErrorResponse(String code, String message, LocalDateTime timestamp) {
        this.code = code;
        this.message = message;
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
    }

    public static ErrorResponse of(ErrorCode errorCode) {
        return ErrorResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static ErrorResponse of(ErrorCode errorCode, String message) {
        return ErrorResponse.builder()
                .code(errorCode.getCode())
                .message(message != null ? message : errorCode.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
    }
}
