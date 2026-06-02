package com.dduru.gildongmu.journey.dto.request;

import com.dduru.gildongmu.journey.domain.enums.ScheduleCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;

public record JourneyScheduleCreateRequest(
        @NotBlank
        @Size(max = 30)
        String title,

        ScheduleCategory category,

        @NotNull
        LocalDate scheduleDate,

        LocalTime startTime,

        LocalTime endTime,

        @NotBlank
        @Size(max = 30)
        String placeName,

        @Size(max = 100)
        String memo,

        String imageUrl
) {
}
