package com.dduru.gildongmu.recommendation.dto.response;

import com.dduru.gildongmu.recommendation.domain.UserRecommendationAvailableDate;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "여행 가능 날짜 범위")
public record AvailableDateResponse(
        @Schema(description = "시작일", example = "2024-08-01")
        LocalDate startDate,
        @Schema(description = "종료일", example = "2024-08-07")
        LocalDate endDate
) {
    public static AvailableDateResponse from(UserRecommendationAvailableDate availableDate) {
        return new AvailableDateResponse(availableDate.getStartDate(), availableDate.getEndDate());
    }
}
