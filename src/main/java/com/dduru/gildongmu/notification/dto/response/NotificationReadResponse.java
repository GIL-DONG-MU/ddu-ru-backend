package com.dduru.gildongmu.notification.dto.response;

public record NotificationReadResponse(boolean success) {

    public static NotificationReadResponse ok() {
        return new NotificationReadResponse(true);
    }
}
