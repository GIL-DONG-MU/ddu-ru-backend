package com.dduru.gildongmu.chat.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ChatDestinationPaths {

    public static final String PUB_ROOM_MESSAGES_PATTERN = "/chat/rooms/{roomId}/messages";
    public static final String TOPIC_ROOM_PREFIX = "/topic/chat/rooms/";

    public static String topicRoom(Long roomId) {
        return TOPIC_ROOM_PREFIX + roomId;
    }
}
