package com.dduru.gildongmu.journey.event;

public record ScheduleCreatedEvent(
        Long scheduleId,
        Long journeyId,
        String scheduleTitle,
        Long actorUserId
) {
}
