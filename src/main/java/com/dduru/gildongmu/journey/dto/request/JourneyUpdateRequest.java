package com.dduru.gildongmu.journey.dto.request;

import java.time.LocalDate;

public record JourneyUpdateRequest(
        String title,
        String photoUrl,
        LocalDate startDate,
        LocalDate endDate
) {
}
