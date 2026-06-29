package com.dduru.gildongmu.participation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record ChatRoomIds(
        @Schema(description = "1:1 연락용 채팅방 ID. 연락 시작 전이면 null입니다.", example = "10", nullable = true)
        Long privateRoomId,
        @Schema(description = "승인 후 입장하는 그룹 채팅방 ID. 승인 전이면 null입니다.", example = "20", nullable = true)
        Long groupRoomId
) {
}
