package com.dduru.gildongmu.chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "채팅방 읽음 처리 응답")
public record ChatReadResponse(
        @Schema(description = "읽음 처리된 채팅방 ID", example = "10")
        Long chatRoomId,

        @Schema(description = "서버에 반영된 마지막 읽음 메시지 ID", example = "120")
        Long lastReadMessageId,

        @Schema(description = "읽음 위치가 실제로 전진했는지 여부. false이면 기존 읽음 위치와 같거나 과거 메시지를 요청한 경우입니다.", example = "true")
        Boolean updated
) {
}
