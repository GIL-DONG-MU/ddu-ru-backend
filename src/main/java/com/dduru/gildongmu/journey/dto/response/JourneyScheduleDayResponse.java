package com.dduru.gildongmu.journey.dto.response;

import java.time.LocalDate;
import java.util.List;

public record JourneyScheduleDayResponse(
        int day,
        LocalDate date,
        List<JourneyScheduleItemResponse> schedules
) {
}
