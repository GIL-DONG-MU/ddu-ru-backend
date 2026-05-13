package com.dduru.gildongmu.home.controller;

import com.dduru.gildongmu.auth.exception.UnauthorizedException;
import com.dduru.gildongmu.common.annotation.CurrentUser;
import com.dduru.gildongmu.common.exception.GlobalExceptionHandler;
import com.dduru.gildongmu.common.time.KoreaTime;
import com.dduru.gildongmu.common.time.TimeProvider;
import com.dduru.gildongmu.home.service.HomeService;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@DisplayName("HomeController 테스트")
class HomeControllerTest {

    @Test
    @DisplayName("GET /api/v1/home 요청에 홈 화면 데이터를 응답한다")
    void retrieveHome() throws Exception {
        MockMvc mockMvc = mockMvcWithUser(10L);

        mockMvc.perform(get("/api/v1/home"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.upcomingTrip.journeyId").value(102))
                .andExpect(jsonPath("$.data.upcomingTrip.dDay").value(12))
                .andExpect(jsonPath("$.data.upcomingTrip.startDate").value("2026-05-25"))
                .andExpect(jsonPath("$.data.popularDestinations.destinations.length()").value(7))
                .andExpect(jsonPath("$.data.popularDestinations.destinations[0].regionName").value("제주도"))
                .andExpect(jsonPath("$.data.mateRecommendation.isAvailable").value(true))
                .andExpect(jsonPath("$.data.mateRecommendation.recommendations[0].recommendationId").value(5001))
                .andExpect(jsonPath("$.data.mateRecommendation.recommendations[0].host.gender").value("F"))
                .andExpect(jsonPath("$.data.superHosts[0].status").value("OPEN"))
                .andExpect(jsonPath("$.data.sameDestinationTrips[0].postId").value(601))
                .andExpect(jsonPath("$.data.sameAgeTrips[0].postId").value(701));
    }

    @Test
    @DisplayName("인증되지 않은 사용자는 UNAUTHORIZED 응답을 받는다")
    void unauthenticatedUserReturnsUnauthorized() throws Exception {
        MockMvc mockMvc = mockMvcWithUser(null);

        mockMvc.perform(get("/api/v1/home"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.data.errorCode").value("UNAUTHORIZED"));
    }

    private MockMvc mockMvcWithUser(Long userId) {
        TimeProvider timeProvider = new TimeProvider(Clock.fixed(
                LocalDateTime.of(2026, 5, 13, 12, 30)
                        .atZone(KoreaTime.ZONE_ID)
                        .toInstant(),
                KoreaTime.ZONE_ID
        ));
        HomeService homeService = new HomeService(timeProvider);
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        return standaloneSetup(new HomeController(homeService))
                .setCustomArgumentResolvers(new FixedCurrentUserArgumentResolver(userId))
                .setMessageConverters(new MappingJackson2HttpMessageConverter(objectMapper))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private static class FixedCurrentUserArgumentResolver implements HandlerMethodArgumentResolver {
        private final Long userId;

        private FixedCurrentUserArgumentResolver(Long userId) {
            this.userId = userId;
        }

        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            return parameter.hasParameterAnnotation(CurrentUser.class);
        }

        @Override
        public Object resolveArgument(
                MethodParameter parameter,
                ModelAndViewContainer mavContainer,
                NativeWebRequest webRequest,
                WebDataBinderFactory binderFactory
        ) {
            if (userId == null) {
                throw new UnauthorizedException();
            }
            return userId;
        }
    }
}
