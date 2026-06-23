package com.dduru.gildongmu.home.service;

import com.dduru.gildongmu.common.time.KoreaTime;
import com.dduru.gildongmu.common.time.TimeProvider;
import com.dduru.gildongmu.home.dto.response.HomePopularDestinationResponse;
import com.dduru.gildongmu.home.dto.response.HomeSuperHostResponse;
import com.dduru.gildongmu.onboarding.repository.UserOnboardingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@DisplayName("HomeService 테스트")
class HomeServiceTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 5, 13, 12, 30);

    private HomeService homeService;

    @BeforeEach
    void setUp() {
        TimeProvider timeProvider = new TimeProvider(Clock.fixed(
                NOW.atZone(KoreaTime.ZONE_ID).toInstant(),
                KoreaTime.ZONE_ID
        ));
        homeService = new HomeService(
                timeProvider,
                mock(UserOnboardingRepository.class)
        );
    }

    @Nested
    @DisplayName("인기 여행지")
    class PopularDestinations {

        @Test
        @DisplayName("mock 인기 여행지 5개를 반환하고 태그 개수 케이스를 포함한다")
        void retrievePopularDestinationsReturnsMockItemsAndTagCases() {
            HomePopularDestinationResponse response = homeService.retrievePopularDestinations();

            assertThat(response.updatedAt()).isEqualTo(NOW);
            assertThat(response.items()).hasSize(5);
            assertThat(response.items().get(0).rank()).isEqualTo(1);
            assertThat(response.items().get(0).destinationId()).isEqualTo(1L);
            assertThat(response.items().get(0).destinationName()).isEqualTo("제주도");
            assertThat(response.items().get(0).availableTripCount()).isEqualTo(14231L);
            assertThat(response.items().get(0).tags()).containsExactly("힐링", "드라이브", "바다");
            assertThat(response.items().get(2).tags()).containsExactly("바다");
            assertThat(response.items().get(3).tags()).isEmpty();
        }
    }

    @Nested
    @DisplayName("슈퍼호스트")
    class SuperHosts {

        @Test
        @DisplayName("mock 슈퍼호스트 5개를 반환하고 hasLiked는 false다")
        void retrieveSuperHostsReturnsMockItemsAndFalseHasLiked() {
            List<HomeSuperHostResponse> response = homeService.retrieveSuperHosts(10L);

            assertThat(response).hasSize(5);
            assertThat(response.get(0).postId()).isEqualTo(501L);
            assertThat(response.get(0).tags()).containsExactly("일출", "등산");
            assertThat(response.get(0).hasLiked()).isFalse();
        }
    }
}
