package com.dduru.gildongmu.notification.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "알림 읽음 처리 응답")
public record NotificationReadResponse(
        @Schema(description = "처리 성공 여부", example = "true")
        boolean success
) {

    public static NotificationReadResponse ok() {
        return new NotificationReadResponse(true);
    }
}
