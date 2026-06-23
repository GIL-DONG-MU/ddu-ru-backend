package com.dduru.gildongmu.notification.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "안 읽은 알림 개수 응답")
public record UnreadCountResponse(
        @Schema(description = "안 읽은 알림 개수", example = "3")
        long unreadCount
) {
}
