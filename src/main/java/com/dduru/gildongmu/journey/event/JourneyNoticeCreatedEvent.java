package com.dduru.gildongmu.journey.event;

public record JourneyNoticeCreatedEvent(
        Long journeyPostId,
        Long journeyId,
        String journeyTitle,
        Long actorUserId
) {
}
