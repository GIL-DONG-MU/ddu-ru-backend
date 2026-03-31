package com.dduru.gildongmu.common.websocket;

import com.dduru.gildongmu.auth.exception.UnauthorizedException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.MimeTypeUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StompErrorHandlerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final StompErrorHandler stompErrorHandler = new StompErrorHandler(objectMapper);

    @Test
    @DisplayName("BusinessException은 JSON ERROR frame으로 변환된다")
    void handleClientMessageProcessingError_businessException_returnsJsonErrorFrame() throws Exception {
        Message<byte[]> clientMessage = connectMessage("receipt-123");

        Message<byte[]> errorMessage = stompErrorHandler.handleClientMessageProcessingError(
                clientMessage,
                new UnauthorizedException("인증이 필요합니다.")
        );

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(errorMessage);
        JsonNode payload = objectMapper.readTree(errorMessage.getPayload());

        assertThat(accessor.getCommand()).isEqualTo(StompCommand.ERROR);
        assertThat(accessor.getReceiptId()).isEqualTo("receipt-123");
        assertThat(accessor.getContentType()).isEqualTo(MimeTypeUtils.APPLICATION_JSON);
        assertThat(payload.get("status").asInt()).isEqualTo(401);
        assertThat(payload.get("data").get("errorCode").asText()).isEqualTo("UNAUTHORIZED");
        assertThat(payload.get("data").get("message").asText()).isEqualTo("인증이 필요합니다.");
    }

    @Test
    @DisplayName("직렬화 실패 시에도 서비스 표준 ErrorResponse 포맷으로 내려간다")
    void handleClientMessageProcessingError_serializationFails_returnsFallbackErrorResponse() throws Exception {
        ObjectMapper failingObjectMapper = mock(ObjectMapper.class);
        when(failingObjectMapper.writeValueAsBytes(org.mockito.ArgumentMatchers.any()))
                .thenThrow(new JsonProcessingException("boom") {
                });
        StompErrorHandler fallbackHandler = new StompErrorHandler(failingObjectMapper);

        Message<byte[]> errorMessage = fallbackHandler.handleClientMessageProcessingError(
                connectMessage("receipt-456"),
                new UnauthorizedException("인증이 필요합니다.")
        );

        JsonNode payload = objectMapper.readTree(errorMessage.getPayload());

        assertThat(payload.get("status").asInt()).isEqualTo(500);
        assertThat(payload.get("data").get("errorCode").asText()).isEqualTo("INTERNAL_SERVER_ERROR");
        assertThat(payload.get("data").has("field")).isFalse();
        assertThat(payload.get("data").get("message").asText()).isEqualTo("서버 오류가 발생했습니다.");
    }

    private static Message<byte[]> connectMessage(String receiptId) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setReceipt(receiptId);
        accessor.setLeaveMutable(true);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }
}
