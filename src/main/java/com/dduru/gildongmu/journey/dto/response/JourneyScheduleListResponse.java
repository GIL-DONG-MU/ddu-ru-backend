package com.dduru.gildongmu.journey.dto.response;

import com.dduru.gildongmu.journey.domain.JourneySchedule;
import com.dduru.gildongmu.post.domain.Post;

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
            Post post,
            List<JourneySchedule> schedules
    ) {
        LocalDate startDate = post.getStartDate();
        LocalDate endDate = post.getEndDate();

        Map<LocalDate, List<JourneyScheduleItemResponse>> schedulesByDate = schedules.stream()
                .collect(Collectors.groupingBy(
                        JourneySchedule::getScheduleDate,
                        Collectors.mapping(JourneyScheduleItemResponse::from, Collectors.toList())
                ));

        List<JourneyScheduleDayResponse> days = new ArrayList<>();
        long totalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        for (int i = 0; i < totalDays; i++) {
            LocalDate date = startDate.plusDays(i);
            List<JourneyScheduleItemResponse> daySchedules = schedulesByDate.getOrDefault(date, List.of());
            days.add(new JourneyScheduleDayResponse(i + 1, date, daySchedules));
        }

        return new JourneyScheduleListResponse(journeyId, startDate, endDate, days);
    }
}
