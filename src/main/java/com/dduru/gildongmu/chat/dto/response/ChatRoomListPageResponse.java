package com.dduru.gildongmu.chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "채팅방 목록 페이지 정보")
public record ChatRoomListPageResponse(
        @Schema(description = "다음 페이지 조회 커서. hasNext=false이면 null", example = "eyJhY3Rpdml0eUF0IjoiMjAyNi0wNi0xOVQxMjozNDo1NiIsImNoYXRSb29tSWQiOjEwfQ==", nullable = true)
        String nextCursor,

        @Schema(description = "다음 페이지 존재 여부", example = "true")
        boolean hasNext
) {
}
