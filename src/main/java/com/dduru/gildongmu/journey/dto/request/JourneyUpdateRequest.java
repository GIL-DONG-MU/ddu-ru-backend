package com.dduru.gildongmu.journey.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record JourneyUpdateRequest(
        @Schema(description = "변경할 여정 제목. 미전달 시 기존 값을 유지합니다.", example = "제주 애월 2박 3일", nullable = true)
        @Size(min = 5, max = 40)
        String title,
        @Schema(description = "변경할 여정 대표 사진 URL. 미전달 시 기존 값을 유지합니다.", example = "https://cdn.example.com/journeys/cover.jpg", nullable = true)
        String photoUrl,
        @Schema(description = "변경할 여행 시작일. endDate와 함께 전달해야 합니다.", example = "2026-07-10", nullable = true)
        @FutureOrPresent(message = "여행 시작일은 오늘 이후여야 합니다")
        LocalDate startDate,
        @Schema(description = "변경할 여행 종료일. startDate와 함께 전달해야 합니다.", example = "2026-07-12", nullable = true)
        @FutureOrPresent(message = "여행 종료일은 오늘 이후여야 합니다")
        LocalDate endDate
) {
    public JourneyUpdateRequest {
        if (title != null) title = title.strip();
        if (photoUrl != null) photoUrl = photoUrl.strip();
    }
}
