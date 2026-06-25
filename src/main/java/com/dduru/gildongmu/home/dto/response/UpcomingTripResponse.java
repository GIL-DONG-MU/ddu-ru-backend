package com.dduru.gildongmu.home.dto.response;

import java.time.LocalDate;

public record UpcomingTripResponse(
        Long journeyId,
        String title,
        int dDay,
        LocalDate startDate,
        LocalDate endDate,
        int currentMemberCount,
        int maxMemberCount,
        int pendingTaskCount
) {
}
