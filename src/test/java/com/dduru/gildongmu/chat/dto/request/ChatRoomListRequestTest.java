package com.dduru.gildongmu.chat.dto.request;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ChatRoomListRequest 테스트")
class ChatRoomListRequestTest {

    @Test
    @DisplayName("cursor는 strip으로 정제한다")
    void stripsCursor() {
        ChatRoomListRequest request = new ChatRoomListRequest(ChatRoomListType.ALL, 20, "  abc  ");

        assertThat(request.cursor()).isEqualTo("abc");
    }

    @Test
    @DisplayName("공백 cursor는 null로 정규화한다")
    void normalizesBlankCursorToNull() {
        ChatRoomListRequest request = new ChatRoomListRequest(ChatRoomListType.ALL, 20, "　 ");

        assertThat(request.cursor()).isNull();
    }
}
