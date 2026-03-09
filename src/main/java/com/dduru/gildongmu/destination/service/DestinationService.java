package com.dduru.gildongmu.destination.service;

import com.dduru.gildongmu.destination.domain.Destination;
import com.dduru.gildongmu.destination.dto.DestinationInfo;
import com.dduru.gildongmu.destination.repository.DestinationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DestinationService {

    private static final int POPULAR_DAYS_LIMIT = 30;

    private static final List<String> FALLBACK_CITY_NAMES = List.of(
            "제주도", "부산", "강릉", "후쿠오카", "오사카",
            "서울", "도쿄", "교토", "방콕", "다낭"
    );

    private final DestinationRepository destinationRepository;

    public List<DestinationInfo> getPopularDestinations() {
        log.debug("인기 여행지 목록 조회 (최근 {}일 기준)", POPULAR_DAYS_LIMIT);
        LocalDateTime since = LocalDateTime.now().minusDays(POPULAR_DAYS_LIMIT);
        List<Long> ids = destinationRepository.findPopularDestinationIds(since);

        if (ids.isEmpty()) {
            log.debug("집계 데이터 없음 - 추천 여행지 10개 반환");
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
        log.debug("인기 여행지 목록 조회 완료 - count={}", result.size());
        return result;
    }

    public List<DestinationInfo> searchDestinations(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            log.debug("여행지 검색 - keyword 없음, 인기 여행지 목록 반환");
            return getPopularDestinations();
        }
        String trimmed = keyword.trim();
        log.debug("여행지 검색 - keyword={}", trimmed);
        List<Destination> destinations = destinationRepository.searchByKeyword(trimmed);
        List<DestinationInfo> result = destinations.stream()
                .map(DestinationInfo::from)
                .toList();
        log.debug("여행지 검색 완료 - keyword={}, count={}", trimmed, result.size());
        return result;
    }
}
