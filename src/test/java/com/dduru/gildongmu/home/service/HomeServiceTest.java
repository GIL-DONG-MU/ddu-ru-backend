package com.dduru.gildongmu.home.service;

import com.dduru.gildongmu.common.time.KoreaTime;
import com.dduru.gildongmu.common.time.TimeProvider;
import com.dduru.gildongmu.home.dto.response.HomePopularDestinationResponse;
import com.dduru.gildongmu.home.dto.response.HomeSuperHostResponse;
import com.dduru.gildongmu.home.exception.HomeSurveyRequiredException;
import com.dduru.gildongmu.onboarding.domain.UserOnboarding;
import com.dduru.gildongmu.onboarding.exception.UserOnboardingNotFoundException;
import com.dduru.gildongmu.onboarding.repository.UserOnboardingRepository;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.domain.enums.OauthType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("HomeService 테스트")
class HomeServiceTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 5, 13, 12, 30);

    private UserOnboardingRepository userOnboardingRepository;
    private HomeService homeService;

    @BeforeEach
    void setUp() {
        TimeProvider timeProvider = new TimeProvider(Clock.fixed(
                NOW.atZone(KoreaTime.ZONE_ID).toInstant(),
                KoreaTime.ZONE_ID
        ));
        userOnboardingRepository = mock(UserOnboardingRepository.class);
        homeService = new HomeService(
                timeProvider,
                userOnboardingRepository
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
    @DisplayName("회원 전용 섹션")
    class MemberOnlySections {

        @Test
        @DisplayName("온보딩 정보가 없는 회원은 예정 여행 섹션을 조회할 수 없다")
        void retrieveUpcomingTripRequiresOnboarding() {
            when(userOnboardingRepository.getByUserIdOrThrow(10L))
                    .thenThrow(new UserOnboardingNotFoundException());

            assertThatThrownBy(() -> homeService.retrieveUpcomingTrip(10L))
                    .isInstanceOf(UserOnboardingNotFoundException.class);
        }

        @Test
        @DisplayName("온보딩 정보가 있는 회원은 같은 여행지 섹션을 조회할 수 있다")
        void retrieveSameDestinationTripsRequiresOnboarding() {
            when(userOnboardingRepository.getByUserIdOrThrow(10L)).thenReturn(onboarding(false));

            assertThat(homeService.retrieveSameDestinationTrips(10L)).hasSize(3);
        }
    }

    @Nested
    @DisplayName("메이트 추천")
    class MateRecommendations {

        @Test
        @DisplayName("설문 미완료 회원은 메이트 추천을 직접 조회할 수 없다")
        void retrieveMateRecommendationsRequiresSurveyCompleted() {
            when(userOnboardingRepository.getByUserIdOrThrow(10L)).thenReturn(onboarding(false));

            assertThatThrownBy(() -> homeService.retrieveMateRecommendations(10L))
                    .isInstanceOf(HomeSurveyRequiredException.class);
        }

        @Test
        @DisplayName("설문 완료 회원은 메이트 추천을 조회할 수 있다")
        void retrieveMateRecommendationsSurveyCompleted() {
            when(userOnboardingRepository.getByUserIdOrThrow(10L)).thenReturn(onboarding(true));

            assertThat(homeService.retrieveMateRecommendations(10L).recommendations()).hasSize(2);
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

    private UserOnboarding onboarding(boolean surveyCompleted) {
        UserOnboarding onboarding = new UserOnboarding(user());
        if (surveyCompleted) {
            onboarding.completeSurvey();
        }
        return onboarding;
    }

    private User user() {
        return User.builder()
                .email("home-service@example.com")
                .name("홈서비스유저")
                .oauthId("home-service-oauth-id")
                .oauthType(OauthType.KAKAO)
                .build();
    }
}
