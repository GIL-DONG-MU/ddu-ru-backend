package com.dduru.gildongmu.destination.service;

import com.dduru.gildongmu.destination.domain.Destination;
import com.dduru.gildongmu.destination.dto.DestinationInfo;
import com.dduru.gildongmu.destination.repository.DestinationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DestinationService 테스트")
class DestinationServiceTest {

    @Mock
    private DestinationRepository destinationRepository;

    @InjectMocks
    private DestinationService destinationService;

    private static final List<String> FALLBACK_CITY_NAMES = List.of(
            "제주도", "부산", "강릉", "후쿠오카", "오사카", "서울", "도쿄", "교토", "방콕", "다낭"
    );

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
    void getPopularDestinations_withData_returnsOrderedByScore() throws Exception {
        when(destinationRepository.findPopularDestinationIds(any())).thenReturn(List.of(2L, 1L));
        Destination first = Destination.builder().countryCode("KR").countryName("대한민국").city("서울").build();
        Destination second = Destination.builder().countryCode("KR").countryName("대한민국").city("부산").build();
        setEntityId(first, 2L);
        setEntityId(second, 1L);
        when(destinationRepository.findAllById(List.of(2L, 1L))).thenReturn(List.of(second, first));

        List<DestinationInfo> result = destinationService.getPopularDestinations();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).city()).isEqualTo("서울");
        assertThat(result.get(1).city()).isEqualTo("부산");
        verify(destinationRepository).findPopularDestinationIds(any());
        verify(destinationRepository).findAllById(List.of(2L, 1L));
    }

    private static void setEntityId(Object entity, Long id) throws Exception {
        java.lang.reflect.Field idField = entity.getClass().getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(entity, id);
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
}
