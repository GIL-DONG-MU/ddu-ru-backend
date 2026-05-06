package com.dduru.gildongmu.journey.dto.response;

import java.util.List;

public record JourneyMainListResponse(
        List<JourneyMainCardResponse> activeJourneys,
        List<JourneyMainCardResponse> completedJourneys
) {
    public static JourneyMainListResponse of(
            List<JourneyMainCardResponse> activeJourneys,
            List<JourneyMainCardResponse> completedJourneys
    ) {
        return new JourneyMainListResponse(activeJourneys, completedJourneys);
    }
}
