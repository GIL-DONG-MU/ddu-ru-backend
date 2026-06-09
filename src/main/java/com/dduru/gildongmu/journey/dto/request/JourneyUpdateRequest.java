package com.dduru.gildongmu.journey.dto.request;

import jakarta.validation.constraints.FutureOrPresent;

import java.time.LocalDate;

public record JourneyUpdateRequest(
        String title,
        String photoUrl,
        @FutureOrPresent(message = "여행 시작일은 오늘 이후여야 합니다")
        LocalDate startDate,
        @FutureOrPresent(message = "여행 종료일은 오늘 이후여야 합니다")
        LocalDate endDate
) {
}
