package com.dduru.gildongmu.journey.dto.response;

import com.dduru.gildongmu.journey.domain.JourneySchedule;
import com.dduru.gildongmu.journey.domain.enums.ScheduleCategory;

import java.time.LocalDate;
import java.time.LocalTime;

public record JourneyScheduleItemResponse(
        Long scheduleId,
        String title,
        ScheduleCategory category,
        LocalDate scheduleDate,
        LocalTime startTime,
        LocalTime endTime,
        String placeName,
        String memo,
        String imageUrl
) {
    public static JourneyScheduleItemResponse from(JourneySchedule schedule) {
        return new JourneyScheduleItemResponse(
                schedule.getId(),
                schedule.getTitle(),
                schedule.getCategory(),
                schedule.getScheduleDate(),
                schedule.getStartTime(),
                schedule.getEndTime(),
                schedule.getPlaceName(),
                schedule.getMemo(),
                schedule.getImageUrl()
        );
    }
}
