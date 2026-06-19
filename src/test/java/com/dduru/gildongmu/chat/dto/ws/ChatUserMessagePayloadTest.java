package com.dduru.gildongmu.chat.dto.ws;

import com.dduru.gildongmu.chat.domain.enums.ChatMessageType;
import com.dduru.gildongmu.chat.exception.ChatSystemMessageSendAccessDeniedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ChatUserMessagePayload 테스트")
class ChatUserMessagePayloadTest {

    @Test
    @DisplayName("TEXT 메시지는 text payload로 변환한다")
    void createsTextPayload() {
        ChatUserMessagePayload payload = ChatUserMessagePayload.from(ChatMessageType.TEXT, "hello");

        assertThat(payload.text()).isEqualTo("hello");
        assertThat(payload.imageUrl()).isNull();
    }

    @Test
    @DisplayName("IMAGE 메시지는 imageUrl payload로 변환한다")
    void createsImagePayload() {
        ChatUserMessagePayload payload = ChatUserMessagePayload.from(ChatMessageType.IMAGE, "https://example.com/a.png");

        assertThat(payload.text()).isNull();
        assertThat(payload.imageUrl()).isEqualTo("https://example.com/a.png");
    }

    @Test
    @DisplayName("SYSTEM 메시지는 비즈니스 예외를 던진다")
    void throwsBusinessExceptionWhenSystemMessage() {
        assertThatThrownBy(() -> ChatUserMessagePayload.from(ChatMessageType.SYSTEM, "system"))
                .isInstanceOf(ChatSystemMessageSendAccessDeniedException.class);
    }
}
