package com.dduru.gildongmu.notification.dto.response;

import com.dduru.gildongmu.notification.domain.Notification;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "알림 목록 조회 응답")
public record NotificationListResponse(
        @Schema(description = "알림 목록")
        List<NotificationInfo> notifications,
        @Schema(description = "다음 페이지 커서 (마지막 페이지면 null)", example = "41")
        Long nextCursor,
        @Schema(description = "다음 페이지 존재 여부", example = "true")
        boolean hasNext,
        @Schema(description = "현재 페이지 알림 수", example = "20")
        int size
) {
    public static NotificationListResponse of(List<Notification> fetched, int requestedSize) {
        boolean hasNext = fetched.size() > requestedSize;
        List<Notification> items = hasNext ? fetched.subList(0, requestedSize) : fetched;
        Long nextCursor = hasNext ? items.get(items.size() - 1).getId() : null;
        return new NotificationListResponse(
                items.stream().map(NotificationInfo::from).toList(),
                nextCursor,
                hasNext,
                items.size()
        );
    }
}
