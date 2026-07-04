package com.dduru.gildongmu.recommendation.dto.query;

import com.dduru.gildongmu.recommendation.domain.UserRecommendationDestinationPreference;
import com.dduru.gildongmu.recommendation.domain.enums.RecommendationDestinationPreferenceType;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public record DestinationPreferenceFilter(
        Set<String> countryCodes,
        Set<Long> destinationIds
) {
    public static DestinationPreferenceFilter from(List<UserRecommendationDestinationPreference> preferences) {
        Set<String> countryCodes = preferences.stream()
                .filter(preference -> preference.getPreferenceType() == RecommendationDestinationPreferenceType.COUNTRY)
                .map(UserRecommendationDestinationPreference::getCountryCode)
                .collect(Collectors.toUnmodifiableSet());
        Set<Long> destinationIds = preferences.stream()
                .filter(preference -> preference.getPreferenceType() == RecommendationDestinationPreferenceType.CITY)
                .map(preference -> preference.getDestination().getId())
                .collect(Collectors.toUnmodifiableSet());
        return new DestinationPreferenceFilter(countryCodes, destinationIds);
    }

    public boolean isEmpty() {
        return countryCodes.isEmpty() && destinationIds.isEmpty();
    }
}
