package com.dduru.gildongmu.recommendation.support;

import com.dduru.gildongmu.recommendation.domain.UserRecommendationAvailableDate;

import java.time.LocalDate;
import java.util.List;

public record AvailableDateRange(
        LocalDate startDate,
        LocalDate endDate
) {
    public static List<AvailableDateRange> from(List<UserRecommendationAvailableDate> availableDates) {
        return availableDates.stream()
                .map(date -> new AvailableDateRange(date.getStartDate(), date.getEndDate()))
                .toList();
    }
}
