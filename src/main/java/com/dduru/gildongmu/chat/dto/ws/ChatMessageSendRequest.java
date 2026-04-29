package com.dduru.gildongmu.chat.dto.ws;

import com.dduru.gildongmu.chat.domain.enums.ChatMessageType;
import jakarta.validation.constraints.NotNull;

/**
 * STOMP 클라이언트 전송 페이로드. {@link ChatMessageType#SYSTEM} 은 서버에서만 생성한다.
 * content 검증은 messageType 에 따라 달라서 서비스 계층의 전용 validator가 담당한다.
 */
public record ChatMessageSendRequest(
        @NotNull(message = "메세지 타입은 필수입니다.")
        ChatMessageType messageType,

        String content
) {
}
