package com.dduru.gildongmu.chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record GroupChatInviteResponse(
        @Schema(description = "그룹 채팅방 ID", example = "1")
        Long roomId,

        @Schema(description = "이번 요청으로 새로 추가된 멤버 수", example = "2")
        int addedMemberCount
) {
}
