package com.dduru.gildongmu.home.dto.response;

import java.time.LocalDate;
import java.util.List;

public record MateRecommendationResponse(
        AvailabilityStatus availabilityStatus,
        int remainingFreeCount,
        List<Item> recommendations
) {
    public static MateRecommendationResponse surveyRequired() {
        return new MateRecommendationResponse(
                AvailabilityStatus.SURVEY_REQUIRED,
                0,
                List.of()
        );
    }

    public static MateRecommendationResponse available(List<Item> recommendations) {
        return new MateRecommendationResponse(
                AvailabilityStatus.AVAILABLE,
                0,
                recommendations
        );
    }

    public enum AvailabilityStatus {
        AVAILABLE,
        SURVEY_REQUIRED
    }

    public record Item(
            Long recommendationId,
            Long postId,
            int matchPercentage,
            String title,
            String location,
            LocalDate startDate,
            LocalDate endDate,
            HomeHostResponse host,
            int currentMemberCount,
            int maxMemberCount,
            String description,
            List<String> tags,
            List<Reason> matchReasons,
            List<Reason> cautionPoints
    ) {
    }

    public record Reason(
            String code,
            String message
    ) {
    }
}
