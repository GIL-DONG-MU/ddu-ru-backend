package com.dduru.gildongmu.participation.event;

public record MatchApprovedEvent(
        Long participationId,
        Long applicantUserId,
        Long journeyId,
        String journeyTitle
) {
}
