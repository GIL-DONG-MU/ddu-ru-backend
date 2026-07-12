package com.dduru.gildongmu.recommendation.dto.request;

import com.dduru.gildongmu.recommendation.domain.enums.RecommendationDestinationPreferenceType;
import jakarta.validation.constraints.NotNull;

public record DestinationPreferenceRequest(
        @NotNull RecommendationDestinationPreferenceType type,
        String countryCode,
        Long destinationId
) {
}
