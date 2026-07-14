package com.dduru.gildongmu.home.service;

import com.dduru.gildongmu.common.time.KoreaTime;
import com.dduru.gildongmu.common.time.TimeProvider;
import com.dduru.gildongmu.common.util.JsonConverter;
import com.dduru.gildongmu.home.dto.response.HomePopularDestinationResponse;
import com.dduru.gildongmu.home.dto.response.HomeSuperHostResponse;
import com.dduru.gildongmu.onboarding.domain.UserOnboarding;
import com.dduru.gildongmu.onboarding.exception.UserOnboardingNotFoundException;
import com.dduru.gildongmu.onboarding.repository.UserOnboardingRepository;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.domain.enums.OauthType;
import com.dduru.gildongmu.profile.utils.ProfileImageResolver;
import com.dduru.gildongmu.recommendation.domain.enums.MateRecommendationBatchStatus;
import com.dduru.gildongmu.recommendation.domain.enums.RecommendationAvailabilityStatus;
import com.dduru.gildongmu.recommendation.dto.query.DestinationPreferenceFilter;
import com.dduru.gildongmu.recommendation.dto.query.MateRecommendationCardQueryResult;
import com.dduru.gildongmu.recommendation.dto.query.RecommendationApplicantContext;
import com.dduru.gildongmu.recommendation.dto.result.DailyMateRecommendationResult;
import com.dduru.gildongmu.recommendation.service.DailyMateRecommendationService;
import com.dduru.gildongmu.recommendation.service.MateRecommendationCardQueryService;
import com.dduru.gildongmu.recommendation.support.RecommendationReasonJsonConverter;
import com.dduru.gildongmu.recommendation.support.TravelTendencyScores;
import com.dduru.gildongmu.post.domain.enums.CompanionType;
import com.dduru.gildongmu.profile.domain.enums.Gender;
import com.dduru.gildongmu.profile.domain.enums.ProfileImageType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("HomeService 테스트")
class HomeServiceTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 5, 13, 12, 30);

    private UserOnboardingRepository userOnboardingRepository;
    private DailyMateRecommendationService dailyMateRecommendationService;
    private MateRecommendationCardQueryService recommendationCardQueryService;
    private HomeService homeService;

    @BeforeEach
    void setUp() {
        TimeProvider timeProvider = new TimeProvider(Clock.fixed(
                NOW.atZone(KoreaTime.ZONE_ID).toInstant(),
                KoreaTime.ZONE_ID
        ));
        userOnboardingRepository = mock(UserOnboardingRepository.class);
        dailyMateRecommendationService = mock(DailyMateRecommendationService.class);
        recommendationCardQueryService = mock(MateRecommendationCardQueryService.class);
        homeService = new HomeService(
                timeProvider,
                userOnboardingRepository,
                dailyMateRecommendationService,
                recommendationCardQueryService,
                new RecommendationReasonJsonConverter(new ObjectMapper()),
                mock(ProfileImageResolver.class),
                new JsonConverter(new ObjectMapper())
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
        @DisplayName("설문 미완료 회원은 SURVEY_REQUIRED 상태를 받는다")
        void retrieveMateRecommendationsRequiresSurveyCompleted() {
            when(dailyMateRecommendationService.getOrCreate(10L))
                    .thenReturn(DailyMateRecommendationResult.surveyRequired());

            assertThat(homeService.retrieveMateRecommendations(10L).availabilityStatus())
                    .isEqualTo(RecommendationAvailabilityStatus.SURVEY_REQUIRED);
        }

        @Test
        @DisplayName("추천 가능하지만 후보가 없으면 AVAILABLE과 빈 목록을 반환한다")
        void retrieveMateRecommendationsSurveyCompleted() {
            when(dailyMateRecommendationService.getOrCreate(10L)).thenReturn(
                    DailyMateRecommendationResult.available(1L, MateRecommendationBatchStatus.EMPTY, null)
            );

            assertThat(homeService.retrieveMateRecommendations(10L).availabilityStatus())
                    .isEqualTo(RecommendationAvailabilityStatus.AVAILABLE);
            assertThat(homeService.retrieveMateRecommendations(10L).recommendations()).isEmpty();
        }

        @Test
        @DisplayName("저장된 추천 이유 객체를 홈 카드 응답에 포함한다")
        void returnsRecommendationReasons() {
            RecommendationApplicantContext context = new RecommendationApplicantContext(
                    NOW.toLocalDate(),
                    Gender.F,
                    27,
                    new DestinationPreferenceFilter(Set.of(), Set.of()),
                    List.of(),
                    new TravelTendencyScores(5, 5, 5, 5)
            );
            when(dailyMateRecommendationService.getOrCreate(10L)).thenReturn(
                    DailyMateRecommendationResult.available(1L, MateRecommendationBatchStatus.COMPLETED, context)
            );
            when(recommendationCardQueryService.findVisibleCards(1L, 10L, context)).thenReturn(List.of(
                    new MateRecommendationCardQueryResult(
                            11L,
                            101L,
                            1,
                            92,
                            "[{\"code\":\"RHYTHM_MATCH\",\"message\":\"여행 리듬이 잘 맞아요\"}]",
                            "[]",
                            "제주 여행 동행 모집",
                            "대한민국",
                            "제주",
                            NOW.toLocalDate().plusDays(5),
                            NOW.toLocalDate().plusDays(7),
                            CompanionType.FULL,
                            2,
                            4,
                            "홈 추천 카드 응답을 검증하기 위한 충분한 길이의 본문입니다.",
                            "[\"힐링\"]",
                            "제주호스트",
                            ProfileImageType.DEFAULT,
                            null,
                            null,
                            null,
                            NOW.toLocalDate().minusYears(30),
                            Gender.M
                    )
            ));

            var response = homeService.retrieveMateRecommendations(10L);

            assertThat(response.recommendations()).hasSize(1);
            assertThat(response.recommendations().get(0).matchReasons())
                    .extracting("code", "message")
                    .containsExactly(tuple("RHYTHM_MATCH", "여행 리듬이 잘 맞아요"));
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
