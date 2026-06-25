package com.dduru.gildongmu.chat.dto.response;

        import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "채팅방 메시지 목록 응답")
public record ChatMessagesResponse(
        @Schema(description = "채팅방 상세 헤더 정보")
        ChatRoomInfoResponse roomInfo,

        @Schema(description = "메시지 목록 페이지 정보")
        ChatMessagePageResponse page,

        @Schema(description = "오래된 순서로 정렬된 메시지 목록")
        List<ChatMessageItemResponse> messages
) {
}
