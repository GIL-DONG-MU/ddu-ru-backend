package com.dduru.gildongmu.journey.dto.query;

import com.dduru.gildongmu.journey.domain.enums.JourneyMemberStatus;

public record JourneyMemberStatusQueryResult(
        Long postId,
        JourneyMemberStatus status
) {
}
