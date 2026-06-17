package com.dduru.gildongmu.journey.dto.response;

import com.dduru.gildongmu.journey.domain.JourneySchedule;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record JourneyScheduleListResponse(
        Long journeyId,
        LocalDate startDate,
        LocalDate endDate,
        List<JourneyScheduleDayResponse> days
) {
    public static JourneyScheduleListResponse of(
            Long journeyId,
            LocalDate startDate,
            LocalDate endDate,
            List<JourneySchedule> schedules
    ) {
        Map<Integer, List<JourneyScheduleItemResponse>> schedulesByOffset = schedules.stream()
                .collect(Collectors.groupingBy(
                        JourneySchedule::getDayOffset,
                        Collectors.mapping(s -> JourneyScheduleItemResponse.from(s, startDate), Collectors.toList())
                ));

        List<JourneyScheduleDayResponse> days = new ArrayList<>();
        long totalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        for (int i = 0; i < totalDays; i++) {
            days.add(new JourneyScheduleDayResponse(i + 1, startDate.plusDays(i), schedulesByOffset.getOrDefault(i, List.of())));
        }

        return new JourneyScheduleListResponse(journeyId, startDate, endDate, days);
    }
}
