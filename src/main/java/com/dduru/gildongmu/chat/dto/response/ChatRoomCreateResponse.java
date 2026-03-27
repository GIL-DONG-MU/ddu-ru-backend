package com.dduru.gildongmu.chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record ChatRoomCreateResponse(
        @Schema(description = "채팅방 ID", example = "1")
        Long roomId,

        @Schema(description = "방 생성 여부", example = "true")
        boolean isCreated
) {
}
