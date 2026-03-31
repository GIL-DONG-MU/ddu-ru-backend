package com.dduru.gildongmu.common.websocket;

import com.dduru.gildongmu.common.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtStompChannelInterceptor implements ChannelInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() == null) {
            return message;
        }

        return switch (accessor.getCommand()) {
            case CONNECT, STOMP -> authenticate(message, accessor);
            case SEND, SUBSCRIBE, UNSUBSCRIBE -> requireAuthenticated(message, accessor);
            default -> message;
        };
    }

    private Message<?> authenticate(Message<?> message, StompHeaderAccessor accessor) {
        String token = resolveToken(accessor);
        if (!StringUtils.hasText(token)) {
            logConnectFailure(accessor, "missing_authorization");
            throw WebSocketAuthException.missingAuthorizationHeader();
        }
        if (!jwtTokenProvider.validateToken(token)) {
            logConnectFailure(accessor, "invalid_token");
            throw WebSocketAuthException.invalidAccessToken();
        }

        Authentication authentication = jwtTokenProvider.resolveAuthentication(token, false);
        if (authentication == null) {
            logConnectFailure(accessor, "authentication_resolution_failed");
            throw WebSocketAuthException.authenticationResolutionFailed();
        }

        accessor.setUser(authentication);
        log.debug("WebSocket CONNECT 인증 완료 - userId={}, sessionId={}",
                authentication.getName(), accessor.getSessionId());
        return message;
    }

    private Message<?> requireAuthenticated(Message<?> message, StompHeaderAccessor accessor) {
        if (accessor.getUser() == null) {
            log.warn("인증되지 않은 WebSocket 요청 차단 - command={}, sessionId={}, destination={}",
                    accessor.getCommand(), accessor.getSessionId(), accessor.getDestination());
            throw WebSocketAuthException.unauthenticatedCommand(accessor.getCommand());
        }
        return message;
    }

    private String resolveToken(StompHeaderAccessor accessor) {
        String bearerToken = accessor.getFirstNativeHeader(AUTHORIZATION_HEADER);
        if (!StringUtils.hasText(bearerToken)) {
            bearerToken = accessor.getFirstNativeHeader(AUTHORIZATION_HEADER.toLowerCase());
        }
        if (!StringUtils.hasText(bearerToken)) {
            return null;
        }
        if (bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }
        return bearerToken;
    }

    private void logConnectFailure(StompHeaderAccessor accessor, String reason) {
        log.warn("WebSocket CONNECT 인증 실패 - reason={}, sessionId={}", reason, accessor.getSessionId());
    }
}
