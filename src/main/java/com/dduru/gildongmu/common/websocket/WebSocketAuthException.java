package com.dduru.gildongmu.common.websocket;

import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;
import org.springframework.messaging.simp.stomp.StompCommand;

public class WebSocketAuthException extends BusinessException {

    private WebSocketAuthException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public static WebSocketAuthException missingAuthorizationHeader() {
        return new WebSocketAuthException(
                ErrorCode.UNAUTHORIZED,
                "WebSocket CONNECT 시 Authorization 헤더가 필요합니다."
        );
    }

    public static WebSocketAuthException invalidAccessToken() {
        return new WebSocketAuthException(
                ErrorCode.INVALID_TOKEN,
                "WebSocket CONNECT 토큰이 유효하지 않습니다."
        );
    }

    public static WebSocketAuthException authenticationResolutionFailed() {
        return new WebSocketAuthException(
                ErrorCode.INVALID_TOKEN,
                "WebSocket CONNECT 인증 정보를 생성할 수 없습니다."
        );
    }

    public static WebSocketAuthException unauthenticatedCommand(StompCommand command) {
        return new WebSocketAuthException(
                ErrorCode.UNAUTHORIZED,
                "인증되지 않은 WebSocket " + command + " 요청입니다."
        );
    }
}
