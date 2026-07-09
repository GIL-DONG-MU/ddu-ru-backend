package com.dduru.gildongmu.recommendation.dto.query;

import com.dduru.gildongmu.recommendation.domain.enums.RecommendationDestinationPreferenceType;

public record DestinationPreferenceFilterRow(
        RecommendationDestinationPreferenceType preferenceType,
        String countryCode,
        Long destinationId
) {
}
