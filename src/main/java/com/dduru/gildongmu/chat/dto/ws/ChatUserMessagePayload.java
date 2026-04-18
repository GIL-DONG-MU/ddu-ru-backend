package com.dduru.gildongmu.chat.dto.ws;

import com.dduru.gildongmu.chat.domain.enums.ChatMessageType;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ChatUserMessagePayload(
        String text,
        String imageUrl
) {

    public static ChatUserMessagePayload from(ChatMessageType messageType, String content) {
        return switch (messageType) {
            case TEXT -> new ChatUserMessagePayload(content, null);
            case IMAGE -> new ChatUserMessagePayload(null, content);
            case SYSTEM -> throw new IllegalArgumentException("SYSTEM 메시지는 사용자 메시지 payload를 만들 수 없습니다.");
        };
    }
}
