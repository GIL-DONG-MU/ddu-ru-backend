package com.dduru.gildongmu.participation.event;

public record MatchAppliedEvent(
        Long participationId,
        Long actorUserId,
        Long recipientUserId,
        String postTitle,
        String actorNickname
) {
}
