package com.dduru.gildongmu.chat.dto.ws;

import com.dduru.gildongmu.chat.domain.enums.ChatMessageType;
import com.dduru.gildongmu.chat.exception.ChatSystemMessageSendAccessDeniedException;
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
            case SYSTEM -> throw new ChatSystemMessageSendAccessDeniedException();
        };
    }
}
