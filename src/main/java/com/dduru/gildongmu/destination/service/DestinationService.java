package com.dduru.gildongmu.destination.service;

import com.dduru.gildongmu.destination.domain.Destination;
import com.dduru.gildongmu.destination.dto.DestinationInfo;
import com.dduru.gildongmu.destination.repository.DestinationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DestinationService {

    private static final List<String> POPULAR_CITY_NAMES = List.of(
            "제주도", "부산", "강릉", "후쿠오카", "오사카"
    );

    private final DestinationRepository destinationRepository;

    public List<DestinationInfo> getPopularDestinations() {
        log.debug("인기 여행지 목록 조회");
        List<Destination> destinations = destinationRepository.findByCityIn(POPULAR_CITY_NAMES);
        List<DestinationInfo> result = destinations.stream()
                .sorted(Comparator.comparingInt(d -> POPULAR_CITY_NAMES.indexOf(d.getCity())))
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
