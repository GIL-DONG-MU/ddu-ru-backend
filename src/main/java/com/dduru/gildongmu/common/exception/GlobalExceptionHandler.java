package com.dduru.gildongmu.common.exception;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.resource.NoResourceFoundException;
import org.springframework.web.servlet.NoHandlerFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        log.error("Business Exception: {}", e.getMessage());
        ErrorCode errorCode = e.getErrorCode();
        ErrorResponse response = ErrorResponse.of(errorCode, e.getMessage());
        return ResponseEntity.status(errorCode.getStatus()).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        log.error("Validation Exception: {}", e.getMessage());
        FieldError fieldError = e.getBindingResult().getFieldErrors().get(0);

        ErrorResponse response = ErrorResponse.ofField(
                ErrorCode.INVALID_INPUT_VALUE,
                fieldError.getField(),
                fieldError.getDefaultMessage()
        );

        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException e) {
        log.error("Constraint Violation Exception: {}", e.getMessage());

        ConstraintViolation<?> violation = e.getConstraintViolations().stream()
                .findFirst()
                .orElse(null);

        String message = violation != null ? violation.getMessage() : "잘못된 요청입니다.";

        String field = null;
        if (violation != null) {
            String path = violation.getPropertyPath().toString();
            // [ex] getPost.postId -> postId
            field = path.contains(".") ? path.substring(path.lastIndexOf(".") + 1) : path;
        }

        ErrorResponse response = ErrorResponse.ofField(ErrorCode.INVALID_INPUT_VALUE, field, message);
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("Illegal Argument Exception: {}", e.getMessage());
        ErrorResponse response = ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, e.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 요청 본문(JSON) 역직렬화 실패 시 처리.
     * Spring이 Jackson의 InvalidFormatException을 HttpMessageNotReadableException으로 감싸서 전달함.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        Throwable cause = e.getCause();
        if (cause instanceof InvalidFormatException ife) {
            return handleInvalidFormatException(ife);
        }
        log.warn("HttpMessageNotReadableException: {}", e.getMessage());
        ErrorResponse response = ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, "요청 본문 형식이 올바르지 않습니다.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    private ResponseEntity<ErrorResponse> handleInvalidFormatException(InvalidFormatException e) {
        String fieldName = e.getPath().isEmpty()
                ? "request"
                : e.getPath().get(e.getPath().size() - 1).getFieldName();

        log.warn("Invalid Format Exception: field={}, value={}, targetType={}",
                fieldName, e.getValue(), e.getTargetType().getSimpleName());

        String message;
        if (e.getTargetType().isEnum()) {
            Object[] enumConstants = e.getTargetType().getEnumConstants();
            String allowed = java.util.stream.Stream.of(enumConstants)
                    .map(Object::toString)
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("");
            message = String.format("%s은(는) [%s] 중 하나여야 합니다. 받은 값: %s", fieldName, allowed, e.getValue());
        } else {
            message = String.format("필드 '%s'의 값 '%s' 형식이 올바르지 않습니다.", fieldName, e.getValue());
        }

        ErrorResponse response = ErrorResponse.ofField(ErrorCode.INVALID_INPUT_VALUE, fieldName, message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<ErrorResponse> handleNotFoundException(Exception e) {
        log.warn("Endpoint Not Found: {}", e.getMessage());
        ErrorResponse response = ErrorResponse.of(ErrorCode.NOT_FOUND, "요청한 API를 찾을 수 없습니다.");
        return ResponseEntity.status(ErrorCode.NOT_FOUND.getStatus()).body(response);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupportedException(HttpRequestMethodNotSupportedException e) {
        log.warn("Method Not Supported: {}", e.getMessage());
        String message = "지원하지 않는 HTTP 메서드입니다: " + e.getMethod();
        ErrorResponse response = ErrorResponse.of(ErrorCode.METHOD_NOT_ALLOWED, message);
        return ResponseEntity.status(ErrorCode.METHOD_NOT_ALLOWED.getStatus()).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Unexpected Exception: ", e);
        ErrorResponse response = ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR);
        return ResponseEntity.internalServerError().body(response);
    }
}
