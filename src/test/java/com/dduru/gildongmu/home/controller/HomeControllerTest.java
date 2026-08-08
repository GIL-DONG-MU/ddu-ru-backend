package com.dduru.gildongmu.home.controller;

import com.dduru.gildongmu.auth.exception.UnauthorizedException;
import com.dduru.gildongmu.common.annotation.CurrentUser;
import com.dduru.gildongmu.common.annotation.OptionalCurrentUser;
import com.dduru.gildongmu.common.exception.ErrorCode;
import com.dduru.gildongmu.common.exception.GlobalExceptionHandler;
import com.dduru.gildongmu.common.time.KoreaTime;
import com.dduru.gildongmu.common.time.TimeProvider;
import com.dduru.gildongmu.common.util.JsonConverter;
import com.dduru.gildongmu.home.mapper.HomeRecommendationMapper;
import com.dduru.gildongmu.home.service.HomeOverviewQueryService;
import com.dduru.gildongmu.home.service.HomePopularDestinationQueryService;
import com.dduru.gildongmu.home.service.HomeRecommendationQueryService;
import com.dduru.gildongmu.home.service.HomeSuperHostQueryService;
import com.dduru.gildongmu.home.service.HomeTripQueryService;
import com.dduru.gildongmu.onboarding.domain.UserOnboarding;
import com.dduru.gildongmu.onboarding.exception.UserOnboardingNotFoundException;
import com.dduru.gildongmu.onboarding.repository.UserOnboardingRepository;
import com.dduru.gildongmu.onboarding.service.OnboardingService;
import com.dduru.gildongmu.profile.utils.ProfileImageResolver;
import com.dduru.gildongmu.recommendation.domain.enums.MateRecommendationBatchStatus;
import com.dduru.gildongmu.recommendation.dto.result.DailyMateRecommendationResult;
import com.dduru.gildongmu.recommendation.service.DailyMateRecommendationService;
import com.dduru.gildongmu.recommendation.service.MateRecommendationCardQueryService;
import com.dduru.gildongmu.recommendation.service.MateRecommendationQueryService;
import com.dduru.gildongmu.recommendation.support.RecommendationReasonJsonConverter;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.domain.enums.OauthType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.time.Clock;
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@DisplayName("HomeController 테스트")
class HomeControllerTest {

    @Test
    @DisplayName("비회원은 disabled 섹션을 포함한 홈 초기 구성을 조회할 수 있다")
    void retrieveHome_guest() throws Exception {
        UserOnboardingRepository userOnboardingRepository = mock(UserOnboardingRepository.class);
        MockMvc mockMvc = mockMvcWithUser(null, userOnboardingRepository);

        mockMvc.perform(get(HomeEndpoints.HOME))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.userAccessStatus").value("GUEST"))
                .andExpect(jsonPath("$.data.sections.length()").value(6))
                .andExpect(jsonPath("$.data.sections[0].key").value("UPCOMING_TRIP"))
                .andExpect(jsonPath("$.data.sections[0].enabled").value(false))
                .andExpect(jsonPath("$.data.sections[0].endpoint").value(HomeEndpoints.UPCOMING_TRIP))
                .andExpect(jsonPath("$.data.sections[0].disabledReason").value("LOGIN_REQUIRED"))
                .andExpect(jsonPath("$.data.sections[1].key").value("POPULAR_DESTINATIONS"))
                .andExpect(jsonPath("$.data.sections[1].enabled").value(true))
                .andExpect(jsonPath("$.data.sections[1].endpoint").value(HomeEndpoints.POPULAR_DESTINATIONS))
                .andExpect(jsonPath("$.data.sections[1].disabledReason").value(nullValue()))
                .andExpect(jsonPath("$.data.sections[2].key").value("MATE_RECOMMENDATIONS"))
                .andExpect(jsonPath("$.data.sections[2].enabled").value(false))
                .andExpect(jsonPath("$.data.sections[2].disabledReason").value("LOGIN_REQUIRED"))
                .andExpect(jsonPath("$.data.sections[3].key").value("SUPER_HOSTS"))
                .andExpect(jsonPath("$.data.sections[3].enabled").value(true))
                .andExpect(jsonPath("$.data.sections[3].endpoint").value(HomeEndpoints.SUPER_HOSTS))
                .andExpect(jsonPath("$.data.sections[4].key").value("SAME_DESTINATION_TRIPS"))
                .andExpect(jsonPath("$.data.sections[4].enabled").value(false))
                .andExpect(jsonPath("$.data.sections[4].endpoint").value(HomeEndpoints.SAME_DESTINATION_TRIPS))
                .andExpect(jsonPath("$.data.sections[4].disabledReason").value("LOGIN_REQUIRED"))
                .andExpect(jsonPath("$.data.sections[5].key").value("SAME_AGE_TRIPS"))
                .andExpect(jsonPath("$.data.sections[5].enabled").value(false))
                .andExpect(jsonPath("$.data.sections[5].disabledReason").value("LOGIN_REQUIRED"));

        verifyNoInteractions(userOnboardingRepository);
    }

    @Test
    @DisplayName("설문 미완료 회원은 메이트 추천 섹션만 설문 필요 상태로 받는다")
    void retrieveHome_memberSurveyRequired() throws Exception {
        UserOnboardingRepository userOnboardingRepository = mock(UserOnboardingRepository.class);
        when(userOnboardingRepository.getByUserIdOrThrow(10L)).thenReturn(new UserOnboarding(user()));
        MockMvc mockMvc = mockMvcWithUser(10L, userOnboardingRepository);

        mockMvc.perform(get(HomeEndpoints.HOME))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.userAccessStatus").value("MEMBER_SURVEY_REQUIRED"))
                .andExpect(jsonPath("$.data.sections.length()").value(6))
                .andExpect(jsonPath("$.data.sections[0].key").value("UPCOMING_TRIP"))
                .andExpect(jsonPath("$.data.sections[0].enabled").value(true))
                .andExpect(jsonPath("$.data.sections[0].disabledReason").value(nullValue()))
                .andExpect(jsonPath("$.data.sections[2].key").value("MATE_RECOMMENDATIONS"))
                .andExpect(jsonPath("$.data.sections[2].enabled").value(false))
                .andExpect(jsonPath("$.data.sections[2].disabledReason").value("SURVEY_REQUIRED"))
                .andExpect(jsonPath("$.data.sections[4].key").value("SAME_DESTINATION_TRIPS"))
                .andExpect(jsonPath("$.data.sections[4].enabled").value(true))
                .andExpect(jsonPath("$.data.sections[5].key").value("SAME_AGE_TRIPS"))
                .andExpect(jsonPath("$.data.sections[5].enabled").value(true));
    }

    @Test
    @DisplayName("설문 완료 회원은 모든 홈 섹션을 호출 가능 상태로 받는다")
    void retrieveHome_memberSurveyCompleted() throws Exception {
        UserOnboarding onboarding = new UserOnboarding(user());
        onboarding.completeSurvey();
        UserOnboardingRepository userOnboardingRepository = mock(UserOnboardingRepository.class);
        when(userOnboardingRepository.getByUserIdOrThrow(10L)).thenReturn(onboarding);
        MockMvc mockMvc = mockMvcWithUser(10L, userOnboardingRepository);

        mockMvc.perform(get(HomeEndpoints.HOME))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.userAccessStatus").value("MEMBER_SURVEY_COMPLETED"))
                .andExpect(jsonPath("$.data.sections.length()").value(6))
                .andExpect(jsonPath("$.data.sections[0].enabled").value(true))
                .andExpect(jsonPath("$.data.sections[1].enabled").value(true))
                .andExpect(jsonPath("$.data.sections[2].enabled").value(true))
                .andExpect(jsonPath("$.data.sections[3].enabled").value(true))
                .andExpect(jsonPath("$.data.sections[4].enabled").value(true))
                .andExpect(jsonPath("$.data.sections[5].enabled").value(true))
                .andExpect(jsonPath("$.data.sections[2].disabledReason").value(nullValue()));
    }

    @Test
    @DisplayName("회원의 온보딩 정보가 없으면 not found 예외가 발생한다")
    void retrieveHome_memberOnboardingNotFound() throws Exception {
        UserOnboardingRepository userOnboardingRepository = mock(UserOnboardingRepository.class);
        when(userOnboardingRepository.getByUserIdOrThrow(10L))
                .thenThrow(new UserOnboardingNotFoundException());
        MockMvc mockMvc = mockMvcWithUser(10L, userOnboardingRepository);

        mockMvc.perform(get(HomeEndpoints.HOME))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.data.errorCode").value(ErrorCode.USER_ONBOARDING_NOT_FOUND.name()))
                .andExpect(jsonPath("$.data.message").value(ErrorCode.USER_ONBOARDING_NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("회원은 예정 여행 섹션을 조회할 수 있다")
    void retrieveUpcomingTrip_member() throws Exception {
        MockMvc mockMvc = mockMvcWithUser(10L, onboardingRepository(false));

        mockMvc.perform(get(HomeEndpoints.UPCOMING_TRIP))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.journeyId").value(102))
                .andExpect(jsonPath("$.data.dDay").value(12))
                .andExpect(jsonPath("$.data.startDate").value("2026-05-25"))
                .andExpect(jsonPath("$.data.pendingTaskCount").value(1));
    }

    @Test
    @DisplayName("비회원도 인기 여행지 섹션을 조회할 수 있다")
    void retrievePopularDestinations_guest() throws Exception {
        MockMvc mockMvc = mockMvcWithUser(null, mock(UserOnboardingRepository.class));

        mockMvc.perform(get(HomeEndpoints.POPULAR_DESTINATIONS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.updatedAt").value("2026-05-13T12:30:00"))
                .andExpect(jsonPath("$.data.items.length()").value(5));
    }

    @Test
    @DisplayName("회원은 메이트 추천 섹션을 조회할 수 있다")
    void retrieveMateRecommendations_member() throws Exception {
        MockMvc mockMvc = mockMvcWithUser(20L, mock(UserOnboardingRepository.class));

        mockMvc.perform(get(HomeEndpoints.MATE_RECOMMENDATIONS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.availabilityStatus").value("AVAILABLE"))
                .andExpect(jsonPath("$.data.remainingFreeCount").value(0))
                .andExpect(jsonPath("$.data.recommendations.length()").value(0));
    }

    @Test
    @DisplayName("설문 미완료 회원은 메이트 추천 섹션에서 SURVEY_REQUIRED 상태를 받는다")
    void retrieveMateRecommendations_memberSurveyRequired() throws Exception {
        MockMvc mockMvc = mockMvcWithUser(
                10L,
                onboardingRepository(false),
                DailyMateRecommendationResult.surveyRequired()
        );

        mockMvc.perform(get(HomeEndpoints.MATE_RECOMMENDATIONS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.availabilityStatus").value("SURVEY_REQUIRED"))
                .andExpect(jsonPath("$.data.remainingFreeCount").value(0))
                .andExpect(jsonPath("$.data.recommendations.length()").value(0));
    }

    @Test
    @DisplayName("비회원도 슈퍼호스트 섹션을 조회할 수 있다")
    void retrieveSuperHosts_guest() throws Exception {
        MockMvc mockMvc = mockMvcWithUser(null, mock(UserOnboardingRepository.class));

        mockMvc.perform(get(HomeEndpoints.SUPER_HOSTS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.length()").value(5))
                .andExpect(jsonPath("$.data[0].postId").value(501))
                .andExpect(jsonPath("$.data[0].tags[0]").value("일출"))
                .andExpect(jsonPath("$.data[0].hasLiked").value(false));
    }

    @Test
    @DisplayName("회원은 같은 여행지 여행 섹션을 조회할 수 있다")
    void retrieveSameDestinationTrips_member() throws Exception {
        MockMvc mockMvc = mockMvcWithUser(10L, onboardingRepository(false));

        mockMvc.perform(get(HomeEndpoints.SAME_DESTINATION_TRIPS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0].postId").value(601))
                .andExpect(jsonPath("$.data[0].startDate").value("2026-05-27"));
    }

    @Test
    @DisplayName("비회원은 같은 여행지 여행 섹션을 조회할 수 없다")
    void retrieveSameDestinationTrips_guest() throws Exception {
        MockMvc mockMvc = mockMvcWithUser(null, mock(UserOnboardingRepository.class));

        mockMvc.perform(get(HomeEndpoints.SAME_DESTINATION_TRIPS))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.data.errorCode").value(ErrorCode.UNAUTHORIZED.name()));
    }

    @Test
    @DisplayName("회원은 또래 여행 섹션을 조회할 수 있다")
    void retrieveSameAgeTrips_member() throws Exception {
        MockMvc mockMvc = mockMvcWithUser(10L, onboardingRepository(false));

        mockMvc.perform(get(HomeEndpoints.SAME_AGE_TRIPS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0].postId").value(701))
                .andExpect(jsonPath("$.data[0].startDate").value("2026-05-26"));
    }

    @Test
    @DisplayName("비회원은 회원 전용 홈 섹션을 조회할 수 없다")
    void retrieveMemberOnlySection_guest() throws Exception {
        MockMvc mockMvc = mockMvcWithUser(null, mock(UserOnboardingRepository.class));

        mockMvc.perform(get(HomeEndpoints.SAME_AGE_TRIPS))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.data.errorCode").value(ErrorCode.UNAUTHORIZED.name()));
    }

    private MockMvc mockMvcWithUser(Long userId, UserOnboardingRepository userOnboardingRepository) {
        return mockMvcWithUser(
                userId,
                userOnboardingRepository,
                DailyMateRecommendationResult.available(1L, MateRecommendationBatchStatus.EMPTY, null)
        );
    }

    private MockMvc mockMvcWithUser(
            Long userId,
            UserOnboardingRepository userOnboardingRepository,
            DailyMateRecommendationResult dailyResult
    ) {
        TimeProvider timeProvider = new TimeProvider(Clock.fixed(
                LocalDateTime.of(2026, 5, 13, 12, 30)
                        .atZone(KoreaTime.ZONE_ID)
                        .toInstant(),
                KoreaTime.ZONE_ID
        ));
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        OnboardingService onboardingService = new OnboardingService(userOnboardingRepository);
        DailyMateRecommendationService dailyMateRecommendationService = mock(DailyMateRecommendationService.class);
        when(dailyMateRecommendationService.getOrCreate(userId)).thenReturn(dailyResult);
        MateRecommendationQueryService mateRecommendationQueryService = new MateRecommendationQueryService(
                dailyMateRecommendationService,
                mock(MateRecommendationCardQueryService.class)
        );
        HomeRecommendationMapper recommendationMapper = new HomeRecommendationMapper(
                new RecommendationReasonJsonConverter(objectMapper),
                mock(ProfileImageResolver.class),
                new JsonConverter(objectMapper)
        );

        return standaloneSetup(new HomeController(
                new HomeOverviewQueryService(onboardingService),
                new HomeTripQueryService(timeProvider, onboardingService),
                new HomePopularDestinationQueryService(timeProvider),
                new HomeRecommendationQueryService(mateRecommendationQueryService, recommendationMapper),
                new HomeSuperHostQueryService(timeProvider)
        ))
                .setCustomArgumentResolvers(new FixedCurrentUserArgumentResolver(userId))
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private UserOnboardingRepository onboardingRepository(boolean surveyCompleted) {
        UserOnboarding onboarding = new UserOnboarding(user());
        if (surveyCompleted) {
            onboarding.completeSurvey();
        }
        UserOnboardingRepository userOnboardingRepository = mock(UserOnboardingRepository.class);
        when(userOnboardingRepository.getByUserIdOrThrow(10L)).thenReturn(onboarding);
        return userOnboardingRepository;
    }

    private User user() {
        return User.builder()
                .email("home@example.com")
                .name("홈유저")
                .oauthId("home-oauth-id")
                .oauthType(OauthType.KAKAO)
                .build();
    }

    private static class FixedCurrentUserArgumentResolver implements HandlerMethodArgumentResolver {
        private final Long userId;

        private FixedCurrentUserArgumentResolver(Long userId) {
            this.userId = userId;
        }

        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            return parameter.hasParameterAnnotation(OptionalCurrentUser.class)
                    || parameter.hasParameterAnnotation(CurrentUser.class);
        }

        @Override
        public Object resolveArgument(
                MethodParameter parameter,
                ModelAndViewContainer mavContainer,
                NativeWebRequest webRequest,
                WebDataBinderFactory binderFactory
        ) {
            if (parameter.hasParameterAnnotation(CurrentUser.class) && userId == null) {
                throw new UnauthorizedException();
            }
            return userId;
        }
    }
}
