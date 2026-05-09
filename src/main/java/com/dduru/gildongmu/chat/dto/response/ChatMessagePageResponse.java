package com.dduru.gildongmu.chat.dto.response;

public record ChatMessagePageResponse(
        int size,
        boolean hasNext,
        Long nextCursor
) {
}
