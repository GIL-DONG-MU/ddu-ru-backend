package com.dduru.gildongmu.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        log.error("Business Exception: {}", e.getMessage());
        ErrorCode errorCode = e.getErrorCode();
        String msg = e.getMessage() != null ? e.getMessage() : errorCode.getMessage();
        ErrorResponse response = ErrorResponse.of(errorCode, msg);
        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        log.error("Validation Exception: {}", e.getMessage());
        String msg = ErrorCode.INVALID_INPUT_VALUE.getMessage();
        if (!e.getBindingResult().getFieldErrors().isEmpty()) {
            msg = e.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        }
        ErrorResponse response = ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, msg);
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("Illegal Argument Exception: {}", e.getMessage());
        ErrorResponse response = ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, e.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Unexpected Exception: ", e);
        ErrorResponse response = ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR);
        return ResponseEntity.internalServerError().body(response);
    }
}
