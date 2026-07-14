package com.dduru.gildongmu.recommendation.controller;

import com.dduru.gildongmu.common.annotation.CurrentUser;
import com.dduru.gildongmu.common.exception.ErrorCode;
import com.dduru.gildongmu.common.exception.GlobalExceptionHandler;
import com.dduru.gildongmu.recommendation.RecommendationEndpoints;
import com.dduru.gildongmu.recommendation.exception.MateRecommendationNotFoundException;
import com.dduru.gildongmu.recommendation.service.MateRecommendationPassService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@DisplayName("MateRecommendationController 테스트")
class MateRecommendationControllerTest {

    @Test
    @DisplayName("본인 추천을 패스하면 204를 반환한다")
    void passesRecommendation() throws Exception {
        MateRecommendationPassService passService = mock(MateRecommendationPassService.class);
        MockMvc mockMvc = mockMvc(passService);

        mockMvc.perform(post(RecommendationEndpoints.MATE_RECOMMENDATIONS + "/11/pass"))
                .andExpect(status().isNoContent());

        verify(passService).pass(1L, 11L);
    }

    @Test
    @DisplayName("본인 추천이 아니면 404를 반환한다")
    void returnsNotFoundForUnownedRecommendation() throws Exception {
        MateRecommendationPassService passService = mock(MateRecommendationPassService.class);
        doThrow(new MateRecommendationNotFoundException()).when(passService).pass(1L, 11L);
        MockMvc mockMvc = mockMvc(passService);

        mockMvc.perform(post(RecommendationEndpoints.MATE_RECOMMENDATIONS + "/11/pass"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.data.errorCode").value(ErrorCode.MATE_RECOMMENDATION_NOT_FOUND.name()));
    }

    private MockMvc mockMvc(MateRecommendationPassService passService) {
        return standaloneSetup(new MateRecommendationController(passService))
                .setCustomArgumentResolvers(new CurrentUserArgumentResolver())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private static class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {
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
            return 1L;
        }
    }
}
