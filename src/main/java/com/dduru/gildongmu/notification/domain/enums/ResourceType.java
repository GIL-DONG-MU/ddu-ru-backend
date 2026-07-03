package com.dduru.gildongmu.notification.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ResourceType {
    JOURNEY("JOURNEY"),
    JOURNEY_POST("JOURNEY_POST"),
    SCHEDULE("SCHEDULE"),
    MATCH("MATCH"),
    CHAT_ROOM("CHAT_ROOM");

    private final String payloadValue;
}
