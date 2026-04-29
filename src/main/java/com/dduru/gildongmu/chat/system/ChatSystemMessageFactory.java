package com.dduru.gildongmu.chat.system;

import com.dduru.gildongmu.chat.dto.ws.ChatSystemMessagePayload;
import com.dduru.gildongmu.common.exception.JsonConvertException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatSystemMessageFactory {

    private final ObjectMapper objectMapper;

    public ChatSystemMessagePayload userInvited(long inviteeUserId, long actorUserId) {
        return ChatSystemMessagePayload.userInvited(inviteeUserId, actorUserId);
    }

    public ChatSystemMessagePayload userLeft(long userId) {
        return ChatSystemMessagePayload.userLeft(userId);
    }

    public ChatSystemMessagePayload userKicked(long targetUserId, long actorUserId) {
        return ChatSystemMessagePayload.userKicked(targetUserId, actorUserId);
    }

    public ChatSystemMessagePayload roomClosed() {
        return ChatSystemMessagePayload.roomClosed();
    }

    public String serialize(ChatSystemMessagePayload payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            log.error("시스템 메시지 직렬화 실패 - payload={}", payload, e);
            throw new JsonConvertException();
        }
    }

    public ChatSystemMessagePayload deserialize(String content) {
        try {
            return objectMapper.readValue(content, ChatSystemMessagePayload.class);
        } catch (JsonProcessingException e) {
            log.error("시스템 메시지 역직렬화 실패 - content={}", content, e);
            throw new JsonConvertException();
        }
    }
}
