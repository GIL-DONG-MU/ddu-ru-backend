package com.dduru.gildongmu.participation.event;

public record MatchRejectedEvent(
        Long participationId,
        Long applicantUserId
) {
}
