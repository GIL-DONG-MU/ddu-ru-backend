package com.dduru.gildongmu.recommendation.support;

import com.dduru.gildongmu.post.domain.enums.CompanionType;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class RecommendationAvailableDateMatcher {

    public boolean matches(
            CompanionType companionType,
            LocalDate postStartDate,
            LocalDate postEndDate,
            List<AvailableDateRange> availableDateRanges
    ) {
        if (availableDateRanges.isEmpty()) {
            return true;
        }

        return availableDateRanges.stream()
                .anyMatch(range -> matches(companionType, postStartDate, postEndDate, range));
    }

    private boolean matches(
            CompanionType companionType,
            LocalDate postStartDate,
            LocalDate postEndDate,
            AvailableDateRange range
    ) {
        return switch (companionType) {
            case FULL -> !range.startDate().isAfter(postStartDate)
                    && !range.endDate().isBefore(postEndDate);
            case PARTIAL -> overlapDays(postStartDate, postEndDate, range) >= 2;
            case MEAL, UNSPECIFIED -> overlapDays(postStartDate, postEndDate, range) >= 1;
        };
    }

    private long overlapDays(
            LocalDate postStartDate,
            LocalDate postEndDate,
            AvailableDateRange range
    ) {
        LocalDate overlapStart = postStartDate.isAfter(range.startDate()) ? postStartDate : range.startDate();
        LocalDate overlapEnd = postEndDate.isBefore(range.endDate()) ? postEndDate : range.endDate();
        if (overlapStart.isAfter(overlapEnd)) {
            return 0;
        }
        return ChronoUnit.DAYS.between(overlapStart, overlapEnd) + 1;
    }
}
