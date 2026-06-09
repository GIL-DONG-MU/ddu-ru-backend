package com.dduru.gildongmu.journey.dto.response;

import com.dduru.gildongmu.journey.domain.Journey;

import java.time.LocalDate;

public record JourneyUpdateResponse(
        Long journeyId,
        String title,
        String photoUrl,
        LocalDate startDate,
        LocalDate endDate
) {
    public static JourneyUpdateResponse from(Journey journey) {
        return new JourneyUpdateResponse(
                journey.getId(),
                journey.getTitle(),
                journey.getPhotoUrl(),
                journey.getPost().getStartDate(),
                journey.getPost().getEndDate()
        );
    }
}
