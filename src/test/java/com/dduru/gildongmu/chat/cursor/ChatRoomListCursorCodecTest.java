package com.dduru.gildongmu.chat.cursor;

import com.dduru.gildongmu.chat.dto.query.ChatRoomListCursor;
import com.dduru.gildongmu.chat.exception.InvalidChatRoomListCursorException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ChatRoomListCursorCodec 테스트")
class ChatRoomListCursorCodecTest {

    private final ChatRoomListCursorCodec codec = new ChatRoomListCursorCodec(new ObjectMapper().findAndRegisterModules());

    @Test
    @DisplayName("cursor를 base64 문자열로 변환하고 다시 복원한다")
    void encodesAndDecodesCursor() {
        ChatRoomListCursor cursor = new ChatRoomListCursor(
                LocalDateTime.of(2026, 6, 17, 12, 0),
                10L
        );

        String encodedCursor = codec.encode(cursor);
        ChatRoomListCursor decodedCursor = codec.decode(encodedCursor);

        assertThat(decodedCursor).isEqualTo(cursor);
    }

    @Test
    @DisplayName("잘못된 cursor 형식은 비즈니스 예외를 던진다")
    void throwsBusinessExceptionWhenCursorInvalid() {
        assertThatThrownBy(() -> codec.decode("invalid-cursor"))
                .isInstanceOf(InvalidChatRoomListCursorException.class);
    }

    @Test
    @DisplayName("필수 값이 없는 cursor는 비즈니스 예외를 던진다")
    void throwsBusinessExceptionWhenRequiredCursorValueMissing() {
        assertThatThrownBy(() -> new ChatRoomListCursor(null, 10L))
                .isInstanceOf(InvalidChatRoomListCursorException.class);
    }
}
