package com.dduru.gildongmu.journey.event;

public record ScheduleUpdatedEvent(
        Long scheduleId,
        Long journeyId,
        String scheduleTitle,
        Long actorUserId
) {
}
