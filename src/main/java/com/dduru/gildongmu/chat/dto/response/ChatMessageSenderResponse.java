package com.dduru.gildongmu.chat.dto.response;

import com.dduru.gildongmu.user.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "채팅 메시지 발신자 정보")
public record ChatMessageSenderResponse(
        @Schema(description = "발신자 회원 ID", example = "33")
        Long userId,

        @Schema(description = "발신자 표시명. 프로필 닉네임이 있으면 닉네임, 없으면 사용자 이름", example = "여행메이트")
        String nickname,

        @Schema(description = "연결 게시글 작성자 여부", example = "false")
        boolean isHost
) {
    public static ChatMessageSenderResponse from(User sender, Long hostUserId) {
        if (sender == null) {
            return null;
        }
        return new ChatMessageSenderResponse(
                sender.getId(),
                displayNameOf(sender),
                sender.getId().equals(hostUserId)
        );
    }

    public static String displayNameOf(User user) {
        if (user.getProfile() != null
                && user.getProfile().getNickname() != null
                && !user.getProfile().getNickname().isBlank()) {
            return user.getProfile().getNickname();
        }
        return user.getName();
    }
}
