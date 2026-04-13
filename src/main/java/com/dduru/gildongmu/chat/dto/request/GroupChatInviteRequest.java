package com.dduru.gildongmu.chat.dto.request;

import jakarta.validation.constraints.NotNull;

public record GroupChatInviteRequest(
        @NotNull(message = "초대할 사용자 ID는 필수입니다")
        Long inviteeUserId
) {
}
