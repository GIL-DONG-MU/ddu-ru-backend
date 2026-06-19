package com.dduru.gildongmu.chat.dto.response;

public record ChatRoomListPageResponse(
        String nextCursor,
        boolean hasNext
) {
}
