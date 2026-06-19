package com.dduru.gildongmu.chat.dto.ws;

import com.dduru.gildongmu.profile.dto.response.ProfileImageInfo;
import com.dduru.gildongmu.profile.utils.ProfileImageResolver;
import com.dduru.gildongmu.user.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "WebSocket 채팅 메시지 발신자 payload")
public record ChatMessageSenderPayload(
        @Schema(description = "발신자 회원 ID", example = "33")
        Long id,

        @Schema(description = "발신자 이름", example = "여행메이트")
        String name,

        @Schema(description = "발신자 프로필 이미지 정보")
        ProfileImageInfo profileImageInfo,

        @Schema(description = "연결 게시글 작성자 여부", example = "false")
        boolean isHost
) {

    public static ChatMessageSenderPayload from(User sender, Long hostUserId, ProfileImageResolver profileImageResolver) {
        return new ChatMessageSenderPayload(
                sender.getId(),
                sender.getName(),
                ProfileImageInfo.from(sender.getProfile(), profileImageResolver),
                sender.getId().equals(hostUserId)
        );
    }
}
