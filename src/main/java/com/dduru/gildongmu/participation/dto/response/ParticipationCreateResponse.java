package com.dduru.gildongmu.participation.dto.response;

import com.dduru.gildongmu.participation.domain.enums.ParticipationStatus;

public record ParticipationCreateResponse(
        Long participationId,
        ParticipationStatus status
) {
}
