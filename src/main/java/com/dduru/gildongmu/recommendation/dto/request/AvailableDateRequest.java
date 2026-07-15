package com.dduru.gildongmu.recommendation.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record AvailableDateRequest(
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate
) {
}
