package com.dduru.gildongmu.chat.dto.query;

public record ChatRoomIdByPostIdQueryResult(
        Long postId,
        Long roomId
) {
}
