package com.dduru.gildongmu.chat.cursor;

import com.dduru.gildongmu.chat.dto.query.ChatRoomListCursor;
import com.dduru.gildongmu.chat.exception.InvalidChatRoomListCursorException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class ChatRoomListCursorCodec {

    private final ObjectMapper objectMapper;

    public String encode(ChatRoomListCursor cursor) {
        try {
            byte[] json = objectMapper.writeValueAsBytes(cursor);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(json);
        } catch (JsonProcessingException e) {
            throw new InvalidChatRoomListCursorException();
        }
    }

    public ChatRoomListCursor decode(String encodedCursor) {
        if (encodedCursor == null || encodedCursor.isBlank()) {
            return null;
        }
        try {
            byte[] json = Base64.getUrlDecoder().decode(encodedCursor);
            return objectMapper.readValue(new String(json, StandardCharsets.UTF_8), ChatRoomListCursor.class);
        } catch (IllegalArgumentException | JsonProcessingException e) {
            throw new InvalidChatRoomListCursorException();
        }
    }
}
