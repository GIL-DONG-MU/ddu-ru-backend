package com.dduru.gildongmu.participation.dto.response;

import com.dduru.gildongmu.participation.domain.Participation;
import com.dduru.gildongmu.participation.domain.enums.ParticipationStatus;

import java.time.LocalDateTime;

public record ParticipationResponse(
        Long participationId,
        Long userId,
        String userName,
        String message,
        ParticipationStatus status,
        LocalDateTime appliedAt,
        LocalDateTime contactedAt,
        LocalDateTime approvedAt,
        LocalDateTime rejectedAt
) {
    public static ParticipationResponse from(Participation participation) {
        return new ParticipationResponse(
                participation.getId(),
                participation.getUser().getId(),
                participation.getUser().getName(),
                participation.getMessage(),
                participation.getStatus(),
                participation.getCreatedAt(),
                participation.getContactedAt(),
                participation.getApprovedAt(),
                participation.getRejectedAt()
        );
    }
}
