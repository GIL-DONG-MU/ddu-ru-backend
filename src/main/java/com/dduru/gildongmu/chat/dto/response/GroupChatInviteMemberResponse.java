package com.dduru.gildongmu.chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "그룹 채팅방 멤버 초대 응답")
public record GroupChatInviteMemberResponse(
        @Schema(description = "그룹 채팅방 ID", example = "20")
        Long roomId,

        @Schema(description = "이번 요청으로 신규 초대되었는지 여부. 이미 멤버였으면 false", example = "true")
        boolean isNewInvitee
) {
}
