package com.dduru.gildongmu.journey.dto.request;

import com.dduru.gildongmu.journey.domain.enums.ScheduleCategory;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;

public record JourneyScheduleUpdateRequest(
        @Size(max = 30)
        String title,

        ScheduleCategory category,

        @Min(0)
        Integer dayOffset,

        LocalTime startTime,

        LocalTime endTime,

        @Size(max = 30)
        String placeName,

        @Size(max = 100)
        String memo,

        String imageUrl
) {
    public JourneyScheduleUpdateRequest {
        if (title != null) title = title.strip();
        if (placeName != null) placeName = placeName.strip();
        if (memo != null) memo = memo.strip();
        if (imageUrl != null) imageUrl = imageUrl.strip();
    }
}
