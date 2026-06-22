package com.dduru.gildongmu.notification.dto.response;

import com.dduru.gildongmu.notification.domain.Notification;
import com.dduru.gildongmu.notification.domain.enums.NotificationType;
import com.dduru.gildongmu.notification.domain.enums.ResourceType;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record NotificationInfo(
        Long notificationId,
        NotificationType type,
        String body,
        ResourceType resourceType,
        Long resourceId,
        boolean isRead,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime createdAt
) {
    public static NotificationInfo from(Notification notification) {
        return new NotificationInfo(
                notification.getId(),
                notification.getType(),
                notification.getBody(),
                notification.getResourceType(),
                notification.getResourceId(),
                notification.isRead(),
                notification.getCreatedAt()
        );
    }
}
