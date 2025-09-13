package com.dduru.gildongmu.participation.dto;

import com.dduru.gildongmu.participation.domain.Participation;
import com.dduru.gildongmu.participation.enums.ParticipationStatus;

import java.time.LocalDateTime;

public record ParticipationResponse(
        Long id,
        Long userId,
        String userName,
        ParticipationStatus status,
        LocalDateTime appliedAt
) {
    public static ParticipationResponse from(Participation participation) {
        return new ParticipationResponse(
                participation.getId(),
                participation.getUser().getId(),
                participation.getUser().getName(),
                participation.getStatus(),
                participation.getCreatedAt()
        );
    }
}
