package com.dduru.gildongmu.destination.service;

import com.dduru.gildongmu.common.time.TimeProvider;
import com.dduru.gildongmu.destination.domain.Destination;
import com.dduru.gildongmu.destination.dto.DestinationInfo;
import com.dduru.gildongmu.destination.dto.DestinationPreferenceSearchResponse;
import com.dduru.gildongmu.destination.repository.DestinationRepository;
import com.dduru.gildongmu.recommendation.domain.enums.RecommendationDestinationPreferenceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DestinationService 테스트")
class DestinationServiceTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 7, 11, 12, 0);

    @Mock
    private DestinationRepository destinationRepository;

    @Mock
    private TimeProvider timeProvider;

    @InjectMocks
    private DestinationService destinationService;

    private static final List<String> FALLBACK_CITY_NAMES = List.of(
            "제주도", "부산", "강릉", "후쿠오카", "오사카", "서울", "도쿄", "교토", "방콕", "다낭"
    );

    @BeforeEach
    void setUpTimeProvider() {
        lenient().when(timeProvider.now()).thenReturn(NOW);
    }

    @DisplayName("집계 데이터가 없으면 추천 여행지 10개 순서대로 반환한다")
    @Test
    void getPopularDestinations_noData_returnsFallbackInOrder() {
        when(destinationRepository.findPopularDestinationIds(any())).thenReturn(List.of());
        Destination jeju = Destination.builder().countryCode("KR").countryName("대한민국").city("제주도").build();
        Destination busan = Destination.builder().countryCode("KR").countryName("대한민국").city("부산").build();
        when(destinationRepository.findByCityIn(FALLBACK_CITY_NAMES)).thenReturn(List.of(busan, jeju));

        List<DestinationInfo> result = destinationService.getPopularDestinations();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).city()).isEqualTo("제주도");
        assertThat(result.get(1).city()).isEqualTo("부산");
        verify(destinationRepository).findByCityIn(FALLBACK_CITY_NAMES);
    }

    @DisplayName("최근 30일 집계가 있으면 점수 순 상위 10개 여행지를 반환한다")
    @Test
    void getPopularDestinations_withData_returnsOrderedByScore() {
        when(destinationRepository.findPopularDestinationIds(any())).thenReturn(List.of(2L, 1L));
        Destination first = Destination.builder().countryCode("KR").countryName("대한민국").city("서울").build();
        Destination second = Destination.builder().countryCode("KR").countryName("대한민국").city("부산").build();
        ReflectionTestUtils.setField(first, "id", 2L);
        ReflectionTestUtils.setField(second, "id", 1L);
        when(destinationRepository.findAllById(List.of(2L, 1L))).thenReturn(List.of(second, first));

        List<DestinationInfo> result = destinationService.getPopularDestinations();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).city()).isEqualTo("서울");
        assertThat(result.get(1).city()).isEqualTo("부산");
        verify(destinationRepository).findPopularDestinationIds(any());
        verify(destinationRepository).findAllById(List.of(2L, 1L));
    }

    @DisplayName("키워드가 없거나 공백이면 인기 여행지 목록을 반환한다")
    @Test
    void searchDestinations_emptyOrBlankKeyword_returnsPopularDestinations() {
        when(destinationRepository.findPopularDestinationIds(any())).thenReturn(List.of());
        Destination jeju = Destination.builder().countryCode("KR").countryName("대한민국").city("제주도").build();
        when(destinationRepository.findByCityIn(FALLBACK_CITY_NAMES)).thenReturn(List.of(jeju));

        List<DestinationInfo> resultEmpty = destinationService.searchDestinations("");
        List<DestinationInfo> resultBlank = destinationService.searchDestinations("   ");
        List<DestinationInfo> resultNull = destinationService.searchDestinations(null);

        assertThat(resultEmpty).hasSize(1);
        assertThat(resultEmpty.get(0).city()).isEqualTo("제주도");
        assertThat(resultBlank).hasSize(1);
        assertThat(resultNull).hasSize(1);
    }

    @DisplayName("키워드가 있으면 검색 결과를 반환한다")
    @Test
    void searchDestinations_withKeyword_returnsSearchResults() {
        String keyword = "제주";
        Destination dest = Destination.builder().countryCode("KR").countryName("대한민국").city("제주도").build();
        when(destinationRepository.searchByKeyword(keyword)).thenReturn(List.of(dest));

        List<DestinationInfo> result = destinationService.searchDestinations(keyword);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).city()).isEqualTo("제주도");
        assertThat(result.get(0).countryName()).isEqualTo("대한민국");
        verify(destinationRepository).searchByKeyword(keyword);
    }

    @DisplayName("키워드 앞뒤 공백은 trim 되어 검색된다")
    @Test
    void searchDestinations_trimmedKeyword() {
        when(destinationRepository.searchByKeyword("오사카")).thenReturn(List.of());

        destinationService.searchDestinations("  오사카  ");

        verify(destinationRepository).searchByKeyword("오사카");
    }

    @DisplayName("선호 여행지 검색은 국가 결과를 먼저 반환하고 도시 결과를 이어서 반환한다")
    @Test
    void searchPreferenceDestinations_returnsCountryFirstThenCities() {
        Destination tokyo = destination("JP", "일본", "도쿄");
        Destination osaka = destination("JP", "일본", "오사카");
        when(destinationRepository.searchByKeyword("일본")).thenReturn(List.of(tokyo, osaka));

        List<DestinationPreferenceSearchResponse> result = destinationService.searchPreferenceDestinations("일본");

        assertThat(result).hasSize(3);
        assertThat(result.get(0).type()).isEqualTo(RecommendationDestinationPreferenceType.COUNTRY);
        assertThat(result.get(0).countryCode()).isEqualTo("JP");
        assertThat(result.get(1).type()).isEqualTo(RecommendationDestinationPreferenceType.CITY);
        assertThat(result.get(2).type()).isEqualTo(RecommendationDestinationPreferenceType.CITY);
    }

    @DisplayName("선호 여행지 검색은 국가 결과를 포함해 최대 20건만 반환한다")
    @Test
    void searchPreferenceDestinations_limitsTotalResultsToTwenty() {
        List<Destination> destinations = IntStream.rangeClosed(1, 20)
                .mapToObj(index -> destination("JP", "일본", "도시" + index))
                .toList();
        when(destinationRepository.searchByKeyword("일본")).thenReturn(destinations);

        List<DestinationPreferenceSearchResponse> result = destinationService.searchPreferenceDestinations("일본");

        assertThat(result).hasSize(20);
        assertThat(result.get(0).type()).isEqualTo(RecommendationDestinationPreferenceType.COUNTRY);
        assertThat(result)
                .filteredOn(response -> response.type() == RecommendationDestinationPreferenceType.CITY)
                .hasSize(19);
    }

    @DisplayName("선호 여행지 검색어가 도시명에만 매칭되면 국가 결과는 포함하지 않는다")
    @Test
    void searchPreferenceDestinations_cityKeywordDoesNotIncludeCountry() {
        Destination busan = destination("KR", "대한민국", "부산");
        when(destinationRepository.searchByKeyword("부산")).thenReturn(List.of(busan));

        List<DestinationPreferenceSearchResponse> result = destinationService.searchPreferenceDestinations("부산");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).type()).isEqualTo(RecommendationDestinationPreferenceType.CITY);
    }

    private Destination destination(String countryCode, String countryName, String city) {
        return Destination.builder()
                .countryCode(countryCode)
                .countryName(countryName)
                .city(city)
                .build();
    }
}
