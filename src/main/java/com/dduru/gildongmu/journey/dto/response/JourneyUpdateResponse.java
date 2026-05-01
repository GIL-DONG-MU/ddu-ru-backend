package com.dduru.gildongmu.journey.dto.response;

import com.dduru.gildongmu.journey.domain.Journey;

public record JourneyUpdateResponse(
        Long journeyId,
        String title,
        String photoUrl
) {
    public static JourneyUpdateResponse from(Journey journey) {
        return new JourneyUpdateResponse(
                journey.getId(),
                journey.getTitle(),
                journey.getPhotoUrl()
        );
    }
}
