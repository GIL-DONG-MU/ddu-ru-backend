package com.dduru.gildongmu.common.websocket;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;
import com.dduru.gildongmu.common.exception.ErrorData;
import com.dduru.gildongmu.common.exception.ErrorResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler;

import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompErrorHandler extends StompSubProtocolErrorHandler {

    private static final String WEBSOCKET_AUTH_REQUIRED_MESSAGE = "WebSocket 인증이 필요합니다.";
    private static final String WEBSOCKET_PROCESSING_ERROR_MESSAGE = "WebSocket 처리 중 오류가 발생했습니다.";
    private static final ErrorResponse FALLBACK_ERROR_RESPONSE = ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR);

    private final ObjectMapper objectMapper;

    @Override
    public Message<byte[]> handleClientMessageProcessingError(@Nullable Message<byte[]> clientMessage, Throwable ex) {
        Throwable cause = unwrap(ex);
        ErrorResponse errorResponse = toErrorResponse(cause);
        StompHeaderAccessor clientAccessor = getClientAccessor(clientMessage);
        StompHeaderAccessor errorAccessor = createErrorAccessor(errorResponse, clientAccessor);
        return handleInternal(errorAccessor, serialize(errorResponse), cause, clientAccessor);
    }

    private Throwable unwrap(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null
                && (current instanceof MessageDeliveryException || current instanceof MessagingException)) {
            current = current.getCause();
        }
        return current;
    }

    private ErrorResponse toErrorResponse(Throwable throwable) {
        if (throwable instanceof BusinessException businessException) {
            return ErrorResponse.of(businessException.getErrorCode(), businessException.getMessage());
        }
        if (throwable instanceof MethodArgumentNotValidException validationException) {
            return fromMethodArgumentNotValid(validationException);
        }
        if (throwable instanceof ConstraintViolationException constraintViolationException) {
            return fromConstraintViolation(constraintViolationException);
        }
        if (throwable instanceof InvalidFormatException invalidFormatException) {
            return fromInvalidFormat(invalidFormatException);
        }
        if (throwable instanceof JsonProcessingException) {
            return ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, "요청 본문 형식이 올바르지 않습니다.");
        }
        if (isAuthenticationFailure(throwable)) {
            return ErrorResponse.of(ErrorCode.UNAUTHORIZED, WEBSOCKET_AUTH_REQUIRED_MESSAGE);
        }
        if (throwable instanceof IllegalArgumentException illegalArgumentException) {
            return ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, illegalArgumentException.getMessage());
        }
        log.error("WebSocket 처리 중 예외 발생", throwable);
        return ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR, WEBSOCKET_PROCESSING_ERROR_MESSAGE);
    }

    private ErrorResponse fromMethodArgumentNotValid(MethodArgumentNotValidException exception) {
        FieldError fieldError = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .orElse(null);
        if (fieldError == null) {
            return ErrorResponse.of(ErrorCode.INVALID_INPUT_VALUE, "잘못된 요청입니다.");
        }
        return ErrorResponse.ofField(
                ErrorCode.INVALID_INPUT_VALUE,
                fieldError.getField(),
                fieldError.getDefaultMessage()
        );
    }

    private ErrorResponse fromConstraintViolation(ConstraintViolationException exception) {
        ConstraintViolation<?> violation = exception.getConstraintViolations().stream()
                .findFirst()
                .orElse(null);

        String field = null;
        String message = "잘못된 요청입니다.";
        if (violation != null) {
            String path = violation.getPropertyPath().toString();
            field = path.contains(".") ? path.substring(path.lastIndexOf('.') + 1) : path;
            message = violation.getMessage();
        }
        return ErrorResponse.ofField(ErrorCode.INVALID_INPUT_VALUE, field, message);
    }

    private ErrorResponse fromInvalidFormat(InvalidFormatException exception) {
        String fieldName = exception.getPath().isEmpty()
                ? "request"
                : exception.getPath().get(exception.getPath().size() - 1).getFieldName();

        String message;
        if (exception.getTargetType().isEnum()) {
            String allowed = Stream.of(exception.getTargetType().getEnumConstants())
                    .map(Object::toString)
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("");
            message = String.format("%s은(는) [%s] 중 하나여야 합니다. 받은 값: %s",
                    fieldName, allowed, exception.getValue());
        } else {
            message = String.format("필드 '%s'의 값 '%s' 형식이 올바르지 않습니다.",
                    fieldName, exception.getValue());
        }

        return ErrorResponse.ofField(ErrorCode.INVALID_INPUT_VALUE, fieldName, message);
    }

    private byte[] serialize(ErrorResponse errorResponse) {
        try {
            return objectMapper.writeValueAsBytes(errorResponse);
        } catch (JsonProcessingException e) {
            log.error("STOMP ERROR payload 직렬화 실패", e);
            return serializeFallback(FALLBACK_ERROR_RESPONSE);
        }
    }

    private StompHeaderAccessor getClientAccessor(@Nullable Message<byte[]> clientMessage) {
        if (clientMessage == null) {
            return null;
        }
        return MessageHeaderAccessor.getAccessor(clientMessage, StompHeaderAccessor.class);
    }

    private StompHeaderAccessor createErrorAccessor(ErrorResponse errorResponse,
                                                    @Nullable StompHeaderAccessor clientAccessor) {
        StompHeaderAccessor errorAccessor = StompHeaderAccessor.create(StompCommand.ERROR);
        errorAccessor.setMessage(errorResponse.data().message());
        errorAccessor.setContentType(MimeTypeUtils.APPLICATION_JSON);
        errorAccessor.setLeaveMutable(true);

        if (clientAccessor != null && clientAccessor.getReceipt() != null) {
            errorAccessor.setReceiptId(clientAccessor.getReceipt());
        }
        return errorAccessor;
    }

    private boolean isAuthenticationFailure(Throwable throwable) {
        return throwable instanceof AuthenticationException || throwable instanceof AccessDeniedException;
    }

    private byte[] serializeFallback(ErrorResponse errorResponse) {
        ErrorData errorData = errorResponse.data();
        String fieldJson = errorData.field() == null
                ? ""
                : ",\"field\":\"" + escapeJson(errorData.field()) + "\"";

        String json = """
                {"status":%d,"data":{"errorCode":"%s"%s,"message":"%s"}}
                """.formatted(
                errorResponse.status(),
                escapeJson(errorData.errorCode()),
                fieldJson,
                escapeJson(errorData.message())
        );

        return json.getBytes(StandardCharsets.UTF_8);
    }

    private String escapeJson(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
