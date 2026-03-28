package com.dduru.gildongmu.chat.dto.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record GroupChatInviteRequest(
        @NotEmpty(message = "초대할 멤버 ID 목록은 비어 있을 수 없습니다.")
        List<Long> inviteeUserIds
) {
    public GroupChatInviteRequest {
        inviteeUserIds = inviteeUserIds == null ? List.of() : List.copyOf(inviteeUserIds);
    }
}
