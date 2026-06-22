package com.dduru.gildongmu.journey.event;

public record ScheduleCanceledEvent(
        Long scheduleId,
        Long journeyId,
        String scheduleTitle,
        Long actorUserId
) {
}
