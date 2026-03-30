package com.dduru.gildongmu.chat.dto.request;

import com.dduru.gildongmu.common.validation.NoNullElements;
import jakarta.validation.constraints.NotEmpty;

import java.util.ArrayList;
import java.util.List;

public record GroupChatInviteRequest(
        @NotEmpty(message = "초대할 멤버 ID 목록은 비어 있을 수 없습니다.")
        @NoNullElements(message = "초대할 멤버 ID 목록에 null이 포함될 수 없습니다.")
        List<Long> inviteeUserIds
) {
    public GroupChatInviteRequest {
        inviteeUserIds = inviteeUserIds == null ? List.of() : new ArrayList<>(inviteeUserIds);
    }
}
