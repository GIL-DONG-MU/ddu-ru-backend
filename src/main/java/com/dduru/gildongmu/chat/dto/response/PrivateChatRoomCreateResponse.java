package com.dduru.gildongmu.chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "1:1 채팅방 생성 또는 기존 방 조회 응답")
public record PrivateChatRoomCreateResponse(
        @Schema(description = "채팅방 ID", example = "1")
        Long roomId,

        @Schema(description = "이번 요청에서 새 채팅방이 생성되었는지 여부. true이면 HTTP 201, false이면 기존 방 조회로 HTTP 200", example = "true")
        boolean isCreated
) {
}
