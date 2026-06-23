package com.dduru.gildongmu.home.controller;

import com.dduru.gildongmu.common.annotation.OptionalCurrentUser;
import com.dduru.gildongmu.common.exception.ErrorCode;
import com.dduru.gildongmu.common.exception.GlobalExceptionHandler;
import com.dduru.gildongmu.home.service.HomeService;
import com.dduru.gildongmu.onboarding.domain.UserOnboarding;
import com.dduru.gildongmu.onboarding.exception.UserOnboardingNotFoundException;
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

        mockMvc.perform(get("/api/v1/home"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.userAccessStatus").value("GUEST"))
                .andExpect(jsonPath("$.data.sections.length()").value(6))
                .andExpect(jsonPath("$.data.sections[0].key").value("UPCOMING_TRIP"))
                .andExpect(jsonPath("$.data.sections[0].enabled").value(false))
                .andExpect(jsonPath("$.data.sections[0].endpoint").value("/api/v1/home/upcoming-trip"))
                .andExpect(jsonPath("$.data.sections[0].disabledReason").value("LOGIN_REQUIRED"))
                .andExpect(jsonPath("$.data.sections[1].key").value("POPULAR_DESTINATIONS"))
                .andExpect(jsonPath("$.data.sections[1].enabled").value(true))
                .andExpect(jsonPath("$.data.sections[1].endpoint").value("/api/v1/home/popular-destinations"))
                .andExpect(jsonPath("$.data.sections[1].disabledReason").value(nullValue()))
                .andExpect(jsonPath("$.data.sections[2].key").value("MATE_RECOMMENDATIONS"))
                .andExpect(jsonPath("$.data.sections[2].enabled").value(false))
                .andExpect(jsonPath("$.data.sections[2].disabledReason").value("LOGIN_REQUIRED"))
                .andExpect(jsonPath("$.data.sections[3].key").value("SUPER_HOSTS"))
                .andExpect(jsonPath("$.data.sections[3].enabled").value(true))
                .andExpect(jsonPath("$.data.sections[3].endpoint").value("/api/v1/home/super-hosts"))
                .andExpect(jsonPath("$.data.sections[4].key").value("SAME_DESTINATION_TRIPS"))
                .andExpect(jsonPath("$.data.sections[4].enabled").value(false))
                .andExpect(jsonPath("$.data.sections[4].endpoint").value("/api/v1/home/same-destination-trips"))
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

        mockMvc.perform(get("/api/v1/home"))
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

        mockMvc.perform(get("/api/v1/home"))
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

        mockMvc.perform(get("/api/v1/home"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.data.errorCode").value(ErrorCode.USER_ONBOARDING_NOT_FOUND.name()))
                .andExpect(jsonPath("$.data.message").value(ErrorCode.USER_ONBOARDING_NOT_FOUND.getMessage()));
    }

    private MockMvc mockMvcWithUser(Long userId, UserOnboardingRepository userOnboardingRepository) {
        HomeService homeService = new HomeService(userOnboardingRepository);
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
