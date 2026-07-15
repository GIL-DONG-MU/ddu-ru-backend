package com.dduru.gildongmu.destination.service;

import com.dduru.gildongmu.common.time.TimeProvider;
import com.dduru.gildongmu.destination.domain.Destination;
import com.dduru.gildongmu.destination.dto.DestinationInfo;
import com.dduru.gildongmu.destination.dto.DestinationPreferenceSearchResponse;
import com.dduru.gildongmu.destination.repository.DestinationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DestinationService {

    private static final int POPULAR_DAYS_LIMIT = 30;
    private static final int PREFERENCE_SEARCH_LIMIT = 20;

    private static final List<String> FALLBACK_CITY_NAMES = List.of(
            "제주도", "부산", "강릉", "후쿠오카", "오사카",
            "서울", "도쿄", "교토", "방콕", "다낭"
    );

    private final DestinationRepository destinationRepository;
    private final TimeProvider timeProvider;

    public List<DestinationInfo> getPopularDestinations() {
        LocalDateTime since = timeProvider.now().minusDays(POPULAR_DAYS_LIMIT);
        List<Long> ids = destinationRepository.findPopularDestinationIds(since);

        if (ids.isEmpty()) {
            List<Destination> fallback = destinationRepository.findByCityIn(FALLBACK_CITY_NAMES);
            return fallback.stream()
                    .sorted(Comparator.comparingInt(d -> FALLBACK_CITY_NAMES.indexOf(d.getCity())))
                    .map(DestinationInfo::from)
                    .toList();
        }

        List<Destination> destinations = destinationRepository.findAllById(ids);
        Map<Long, Destination> byId = destinations.stream().collect(Collectors.toMap(Destination::getId, d -> d));
        List<DestinationInfo> result = ids.stream()
                .map(byId::get)
                .filter(Objects::nonNull)
                .map(DestinationInfo::from)
                .toList();
        return result;
    }

    public List<DestinationInfo> searchDestinations(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return getPopularDestinations();
        }
        String trimmed = keyword.trim();
        List<Destination> destinations = destinationRepository.searchByKeyword(trimmed);
        return destinations.stream()
                .map(DestinationInfo::from)
                .toList();
    }

    public List<DestinationPreferenceSearchResponse> searchPreferenceDestinations(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return List.of();
        }
        String trimmedKeyword = keyword.trim();
        List<Destination> matchedDestinations = destinationRepository.searchByKeyword(trimmedKeyword);

        List<DestinationPreferenceSearchResponse> searchResponses = new ArrayList<>();
        searchResponses.addAll(extractCountryResponses(matchedDestinations, trimmedKeyword));
        searchResponses.addAll(extractCityResponses(matchedDestinations));

        return searchResponses.stream()
                .limit(PREFERENCE_SEARCH_LIMIT)
                .toList();
    }

    private List<DestinationPreferenceSearchResponse> extractCountryResponses(
            List<Destination> destinations,
            String keyword
    ) {
        Map<String, Destination> countryDestinationByCode = new LinkedHashMap<>();
        for (Destination destination : destinations) {
            if (destination.getCountryName().contains(keyword)) {
                countryDestinationByCode.putIfAbsent(destination.getCountryCode(), destination);
            }
        }

        return countryDestinationByCode.values().stream()
                .map(DestinationPreferenceSearchResponse::country)
                .toList();
    }

    private List<DestinationPreferenceSearchResponse> extractCityResponses(List<Destination> destinations) {
        return destinations.stream()
                .map(DestinationPreferenceSearchResponse::city)
                .toList();
    }
}
