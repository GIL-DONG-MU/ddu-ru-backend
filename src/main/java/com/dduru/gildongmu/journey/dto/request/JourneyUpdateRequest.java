package com.dduru.gildongmu.journey.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record JourneyUpdateRequest(
        @Size(min = 5, max = 40)
        String title,
        String photoUrl,
        @FutureOrPresent(message = "여행 시작일은 오늘 이후여야 합니다")
        LocalDate startDate,
        @FutureOrPresent(message = "여행 종료일은 오늘 이후여야 합니다")
        LocalDate endDate
) {
    public JourneyUpdateRequest {
        if (title != null) title = title.strip();
        if (photoUrl != null) photoUrl = photoUrl.strip();
    }
}
