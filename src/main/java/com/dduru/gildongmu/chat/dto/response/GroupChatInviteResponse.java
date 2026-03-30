package com.dduru.gildongmu.chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record GroupChatInviteResponse(
        @Schema(description = "그룹 채팅방 ID", example = "1")
        Long roomId,

        @Schema(description = "이번 요청으로 새로 추가된 멤버 수", example = "2")
        int addedMemberCount,

        @Schema(description = "존재하지 않아 초대되지 않은 사용자 ID 목록", example = "[9999, 10000]")
        List<Long> missingUserIds,

        @Schema(description = "이미 멤버여서 초대되지 않은 사용자 ID 목록", example = "[3, 7]")
        List<Long> alreadyMemberUserIds
) {
    public static GroupChatInviteResponse empty(Long roomId) {
        return new GroupChatInviteResponse(roomId, 0, List.of(), List.of());
    }

    public static GroupChatInviteResponse empty(Long roomId, InviteTargetsResult resolution) {
        return new GroupChatInviteResponse(roomId, 0, resolution.missingUserIds(), resolution.alreadyMemberUserIds());
    }

    public static GroupChatInviteResponse success(Long roomId, int size, InviteTargetsResult resolution) {
        return new GroupChatInviteResponse(roomId, size, resolution.missingUserIds(), resolution.alreadyMemberUserIds());
    }
}
