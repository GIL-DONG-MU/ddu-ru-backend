package com.dduru.gildongmu.common.websocket;

import com.dduru.gildongmu.common.jwt.JwtTokenProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.security.Principal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtStompChannelInterceptorTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private JwtStompChannelInterceptor interceptor;

    @Test
    @DisplayName("CONNECT에서 유효한 JWT를 보내면 Principal이 세션에 저장된다")
    void preSend_connectWithValidToken_setsPrincipal() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "1",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        Message<byte[]> message = stompMessage(StompCommand.CONNECT, "Authorization", "Bearer valid-token", null);

        when(jwtTokenProvider.validateToken("valid-token")).thenReturn(true);
        when(jwtTokenProvider.resolveAuthentication("valid-token", false)).thenReturn(authentication);

        Message<?> result = interceptor.preSend(message, mock(MessageChannel.class));

        Principal principal = StompHeaderAccessor.wrap(result).getUser();
        assertThat(principal).isEqualTo(authentication);
    }

    @Test
    @DisplayName("CONNECT에서 Authorization 헤더가 없으면 예외가 발생한다")
    void preSend_connectWithoutAuthorization_throwsUnauthorized() {
        Message<byte[]> message = stompMessage(StompCommand.CONNECT, null, null, null);

        assertThatThrownBy(() -> interceptor.preSend(message, mock(MessageChannel.class)))
                .isInstanceOf(WebSocketAuthException.class)
                .hasMessage("WebSocket CONNECT 시 Authorization 헤더가 필요합니다.");
    }

    @Test
    @DisplayName("CONNECT에서 유효하지 않은 JWT를 보내면 예외가 발생한다")
    void preSend_connectWithInvalidToken_throwsInvalidToken() {
        Message<byte[]> message = stompMessage(StompCommand.CONNECT, "Authorization", "Bearer invalid-token", null);

        when(jwtTokenProvider.validateToken("invalid-token")).thenReturn(false);

        assertThatThrownBy(() -> interceptor.preSend(message, mock(MessageChannel.class)))
                .isInstanceOf(WebSocketAuthException.class)
                .hasMessage("WebSocket CONNECT 토큰이 유효하지 않습니다.");
    }

    @Test
    @DisplayName("인증되지 않은 세션의 SEND 요청은 차단한다")
    void preSend_sendWithoutPrincipal_throwsUnauthorized() {
        Message<byte[]> message = stompMessage(StompCommand.SEND, null, null, null);

        assertThatThrownBy(() -> interceptor.preSend(message, mock(MessageChannel.class)))
                .isInstanceOf(WebSocketAuthException.class)
                .hasMessage("인증되지 않은 WebSocket SEND 요청입니다.");
    }

    private static Message<byte[]> stompMessage(
            StompCommand command,
            String headerName,
            String headerValue,
            Principal principal
    ) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(command);
        if (headerName != null && headerValue != null) {
            accessor.setNativeHeader(headerName, headerValue);
        }
        if (principal != null) {
            accessor.setUser(principal);
        }
        accessor.setLeaveMutable(true);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }
}
