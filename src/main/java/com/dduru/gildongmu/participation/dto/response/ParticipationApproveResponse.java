package com.dduru.gildongmu.participation.dto.response;

import com.dduru.gildongmu.participation.domain.enums.ParticipationStatus;

public record ParticipationApproveResponse(
        Long participationId,
        Long inviteeUserId,
        Long groupRoomId,
        ParticipationStatus status
) {
}
