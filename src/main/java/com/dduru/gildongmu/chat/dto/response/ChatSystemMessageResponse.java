package com.dduru.gildongmu.chat.dto.response;

import com.dduru.gildongmu.chat.constants.ChatMessageConstants;
import com.dduru.gildongmu.chat.dto.ws.ChatSystemMessagePayload;
import com.dduru.gildongmu.chat.system.ChatSystemMessageType;
import com.dduru.gildongmu.user.domain.User;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

@Schema(description = "시스템 메시지 표시 정보")
public record ChatSystemMessageResponse(
        @Schema(description = "시스템 메시지 타입", example = "USER_INVITED", allowableValues = {"USER_INVITED", "USER_LEFT", "USER_KICKED", "ROOM_CLOSED"})
        ChatSystemMessageType type,

        @Schema(description = "클라이언트가 그대로 표시할 수 있는 시스템 메시지 문구", example = "여행메이트 님이 그룹 채팅방에 참여했습니다.")
        String displayText,

        @Schema(description = "액션 수행자 회원 ID. 초대/내보내기 이벤트에서 사용", example = "1", nullable = true)
        Long actorUserId,

        @Schema(description = "초대된 회원 ID. USER_INVITED에서 사용", example = "33", nullable = true)
        Long inviteeUserId,

        @Schema(description = "채팅방을 나간 회원 ID. USER_LEFT에서 사용", example = "33", nullable = true)
        Long userId,

        @Schema(description = "내보내진 회원 ID. USER_KICKED에서 사용", example = "33", nullable = true)
        Long targetUserId
) {
    public static ChatSystemMessageResponse from(
            ChatSystemMessagePayload payload,
            Map<Long, User> systemUsers
    ) {
        return new ChatSystemMessageResponse(
                payload.type(),
                displayText(payload, systemUsers),
                payload.actorUserId(),
                payload.inviteeUserId(),
                payload.userId(),
                payload.targetUserId()
        );
    }

    private static String displayText(ChatSystemMessagePayload payload, Map<Long, User> systemUsers) {
        String template = payload.type().getDisplayTextTemplate();
        Long displayUserId = switch (payload.type()) {
            case USER_INVITED -> payload.inviteeUserId();
            case USER_LEFT -> payload.userId();
            case USER_KICKED -> payload.targetUserId();
            case ROOM_CLOSED -> null;
        };
        if (displayUserId == null) {
            return template;
        }
        return template.formatted(displayNickname(displayUserId, systemUsers));
    }

    private static String displayNickname(Long userId, Map<Long, User> users) {
        if (userId == null) {
            return ChatMessageConstants.UNKNOWN_NICKNAME;
        }
        User user = users.get(userId);
        if (user == null) {
            return ChatMessageConstants.UNKNOWN_NICKNAME;
        }
        return ChatMessageSenderResponse.displayNameOf(user);
    }
}
