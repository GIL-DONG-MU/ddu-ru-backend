package com.dduru.gildongmu.notification.dto.response;

import com.dduru.gildongmu.notification.domain.Notification;

import java.util.List;

public record NotificationListResponse(
        List<NotificationInfo> notifications,
        Long nextCursor,
        boolean hasNext,
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
