package com.dduru.gildongmu.home.controller;

import com.dduru.gildongmu.common.annotation.OptionalCurrentUser;
import com.dduru.gildongmu.common.exception.GlobalExceptionHandler;
import com.dduru.gildongmu.common.time.KoreaTime;
import com.dduru.gildongmu.common.time.TimeProvider;
import com.dduru.gildongmu.home.service.HomeService;
import com.dduru.gildongmu.onboarding.domain.UserOnboarding;
import com.dduru.gildongmu.onboarding.repository.UserOnboardingRepository;
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
import java.util.Optional;

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
    @DisplayName("비회원도 홈 화면 공통 데이터를 조회할 수 있다")
    void retrieveHome_guest() throws Exception {
        UserOnboardingRepository userOnboardingRepository = mock(UserOnboardingRepository.class);
        MockMvc mockMvc = mockMvcWithUser(null, userOnboardingRepository);

        mockMvc.perform(get("/api/v1/home"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.viewerStatus").value("GUEST"))
                .andExpect(jsonPath("$.data.upcomingTrip").doesNotExist())
                .andExpect(jsonPath("$.data.popularDestinations.destinations.length()").value(7))
                .andExpect(jsonPath("$.data.popularDestinations.destinations[0].regionName").value("제주도"))
                .andExpect(jsonPath("$.data.mateRecommendation").doesNotExist())
                .andExpect(jsonPath("$.data.superHosts[0].status").value("OPEN"))
                .andExpect(jsonPath("$.data.sameDestinationTrips[0].postId").value(601))
                .andExpect(jsonPath("$.data.sameAgeTrips").doesNotExist());

        verifyNoInteractions(userOnboardingRepository);
    }

    @Test
    @DisplayName("설문 미완료 회원은 계정 기반 개인화 데이터만 조회한다")
    void retrieveHome_memberSurveyRequired() throws Exception {
        UserOnboardingRepository userOnboardingRepository = mock(UserOnboardingRepository.class);
        when(userOnboardingRepository.findByUser_Id(10L)).thenReturn(Optional.of(new UserOnboarding(user())));
        MockMvc mockMvc = mockMvcWithUser(10L, userOnboardingRepository);

        mockMvc.perform(get("/api/v1/home"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.viewerStatus").value("MEMBER_SURVEY_REQUIRED"))
                .andExpect(jsonPath("$.data.upcomingTrip.journeyId").value(102))
                .andExpect(jsonPath("$.data.upcomingTrip.dDay").value(12))
                .andExpect(jsonPath("$.data.mateRecommendation").doesNotExist())
                .andExpect(jsonPath("$.data.superHosts[0].status").value("OPEN"))
                .andExpect(jsonPath("$.data.sameDestinationTrips[0].postId").value(601))
                .andExpect(jsonPath("$.data.sameAgeTrips[0].postId").value(701));
    }

    @Test
    @DisplayName("설문 완료 회원은 홈 화면 전체 개인화 데이터를 조회한다")
    void retrieveHome_memberSurveyCompleted() throws Exception {
        UserOnboarding onboarding = new UserOnboarding(user());
        onboarding.completeSurvey();
        UserOnboardingRepository userOnboardingRepository = mock(UserOnboardingRepository.class);
        when(userOnboardingRepository.findByUser_Id(10L)).thenReturn(Optional.of(onboarding));
        MockMvc mockMvc = mockMvcWithUser(10L, userOnboardingRepository);

        mockMvc.perform(get("/api/v1/home"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.viewerStatus").value("MEMBER_SURVEY_COMPLETED"))
                .andExpect(jsonPath("$.data.upcomingTrip.journeyId").value(102))
                .andExpect(jsonPath("$.data.upcomingTrip.startDate").value("2026-05-25"))
                .andExpect(jsonPath("$.data.mateRecommendation.isAvailable").value(true))
                .andExpect(jsonPath("$.data.mateRecommendation.recommendations[0].recommendationId").value(5001))
                .andExpect(jsonPath("$.data.mateRecommendation.recommendations[0].host.gender").value("F"))
                .andExpect(jsonPath("$.data.superHosts[0].status").value("OPEN"))
                .andExpect(jsonPath("$.data.sameDestinationTrips[0].postId").value(601))
                .andExpect(jsonPath("$.data.sameAgeTrips[0].postId").value(701));
    }

    private MockMvc mockMvcWithUser(Long userId, UserOnboardingRepository userOnboardingRepository) {
        TimeProvider timeProvider = new TimeProvider(Clock.fixed(
                LocalDateTime.of(2026, 5, 13, 12, 30)
                        .atZone(KoreaTime.ZONE_ID)
                        .toInstant(),
                KoreaTime.ZONE_ID
        ));
        HomeService homeService = new HomeService(timeProvider, userOnboardingRepository);
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        return standaloneSetup(new HomeController(homeService))
                .setCustomArgumentResolvers(new FixedCurrentUserArgumentResolver(userId))
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
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
            return parameter.hasParameterAnnotation(OptionalCurrentUser.class);
        }

        @Override
        public Object resolveArgument(
                MethodParameter parameter,
                ModelAndViewContainer mavContainer,
                NativeWebRequest webRequest,
                WebDataBinderFactory binderFactory
        ) {
            return userId;
        }
    }
}
