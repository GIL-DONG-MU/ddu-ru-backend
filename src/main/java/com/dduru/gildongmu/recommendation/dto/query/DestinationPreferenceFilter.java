package com.dduru.gildongmu.recommendation.dto.query;

import com.dduru.gildongmu.recommendation.domain.enums.RecommendationDestinationPreferenceType;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public record DestinationPreferenceFilter(
        Set<String> countryCodes,
        Set<Long> destinationIds
) {
    public static DestinationPreferenceFilter from(List<DestinationPreferenceFilterRow> rows) {
        Set<String> countryCodes = rows.stream()
                .filter(row -> row.preferenceType() == RecommendationDestinationPreferenceType.COUNTRY)
                .map(DestinationPreferenceFilterRow::countryCode)
                .filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableSet());
        Set<Long> destinationIds = rows.stream()
                .filter(row -> row.preferenceType() == RecommendationDestinationPreferenceType.CITY)
                .map(DestinationPreferenceFilterRow::destinationId)
                .filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableSet());
        return new DestinationPreferenceFilter(countryCodes, destinationIds);
    }

    public boolean isEmpty() {
        return countryCodes.isEmpty() && destinationIds.isEmpty();
    }
}
