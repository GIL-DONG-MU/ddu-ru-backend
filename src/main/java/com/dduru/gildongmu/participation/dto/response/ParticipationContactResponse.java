package com.dduru.gildongmu.participation.dto.response;

import com.dduru.gildongmu.participation.domain.enums.ParticipationStatus;

public record ParticipationContactResponse(
        Long participationId,
        Long participantUserId,
        Long privateRoomId,
        boolean roomCreated,
        ParticipationStatus status
) {
}
