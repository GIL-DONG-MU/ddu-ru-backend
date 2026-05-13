package com.dduru.gildongmu.journey.controller;

import com.dduru.gildongmu.auth.exception.UnauthorizedException;
import com.dduru.gildongmu.common.annotation.CurrentUser;
import com.dduru.gildongmu.common.exception.GlobalExceptionHandler;
import com.dduru.gildongmu.journey.dto.request.JourneyPostCommentCreateRequest;
import com.dduru.gildongmu.journey.dto.request.JourneyPostCommentUpdateRequest;
import com.dduru.gildongmu.journey.dto.response.JourneyPostCommentListResponse;
import com.dduru.gildongmu.journey.dto.response.JourneyPostCommentResponse;
import com.dduru.gildongmu.journey.service.JourneyPostCommentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
@DisplayName("JourneyPostCommentController 테스트")
class JourneyPostCommentControllerTest {

    @Mock
    private JourneyPostCommentService journeyPostCommentService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = mockMvcWithUser(10L);
    }

    @Test
    @DisplayName("GET /api/v1/journeys/{journeyId}/posts/{journeyPostId}/comments 요청을 service에 위임한다")
    void retrieveComments() throws Exception {
        Long journeyId = 1L;
        Long journeyPostId = 101L;
        JourneyPostCommentListResponse response = new JourneyPostCommentListResponse(
                journeyPostId,
                5L,
                true,
                List.of()
        );
        when(journeyPostCommentService.retrieveComments(journeyId, journeyPostId, 10L, 2))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/journeys/{journeyId}/posts/{journeyPostId}/comments", journeyId, journeyPostId)
                        .param("limit", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.journeyPostId").value(101))
                .andExpect(jsonPath("$.data.commentCount").value(5))
                .andExpect(jsonPath("$.data.hasMore").value(true));

        verify(journeyPostCommentService).retrieveComments(journeyId, journeyPostId, 10L, 2);
    }

    @Test
    @DisplayName("POST /api/v1/journeys/{journeyId}/posts/{journeyPostId}/comments 요청을 service에 위임한다")
    void createComment() throws Exception {
        Long journeyId = 1L;
        Long journeyPostId = 101L;
        JourneyPostCommentResponse response = new JourneyPostCommentResponse(
                11L,
                journeyPostId,
                null,
                "네 맞춰서 도착할게요!",
                LocalDateTime.of(2026, 5, 13, 15, 0),
                LocalDateTime.of(2026, 5, 13, 15, 0),
                true
        );
        when(journeyPostCommentService.createComment(eq(journeyId), eq(journeyPostId), eq(10L), any(JourneyPostCommentCreateRequest.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/journeys/{journeyId}/posts/{journeyPostId}/comments", journeyId, journeyPostId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"content":"네 맞춰서 도착할게요!"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.commentId").value(11))
                .andExpect(jsonPath("$.data.content").value("네 맞춰서 도착할게요!"));

        ArgumentCaptor<JourneyPostCommentCreateRequest> requestCaptor =
                ArgumentCaptor.forClass(JourneyPostCommentCreateRequest.class);
        verify(journeyPostCommentService).createComment(eq(journeyId), eq(journeyPostId), eq(10L), requestCaptor.capture());
        assertThat(requestCaptor.getValue().content()).isEqualTo("네 맞춰서 도착할게요!");
    }

    @Test
    @DisplayName("댓글 내용이 없으면 INVALID_INPUT_VALUE 응답을 받는다")
    void invalidCreateRequestReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/journeys/{journeyId}/posts/{journeyPostId}/comments", 1L, 101L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.errorCode").value("INVALID_INPUT_VALUE"))
                .andExpect(jsonPath("$.data.field").value("content"));

        verifyNoInteractions(journeyPostCommentService);
    }

    @Test
    @DisplayName("PATCH /api/v1/journeys/{journeyId}/posts/{journeyPostId}/comments/{commentId} 요청을 service에 위임한다")
    void updateComment() throws Exception {
        Long journeyId = 1L;
        Long journeyPostId = 101L;
        Long commentId = 11L;
        JourneyPostCommentResponse response = new JourneyPostCommentResponse(
                commentId,
                journeyPostId,
                null,
                "10분 일찍 도착할게요!",
                LocalDateTime.of(2026, 5, 13, 15, 0),
                LocalDateTime.of(2026, 5, 13, 15, 10),
                true
        );
        when(journeyPostCommentService.updateComment(eq(journeyId), eq(journeyPostId), eq(commentId), eq(10L), any(JourneyPostCommentUpdateRequest.class)))
                .thenReturn(response);

        mockMvc.perform(patch("/api/v1/journeys/{journeyId}/posts/{journeyPostId}/comments/{commentId}", journeyId, journeyPostId, commentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"content":"10분 일찍 도착할게요!"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.commentId").value(11))
                .andExpect(jsonPath("$.data.content").value("10분 일찍 도착할게요!"));
    }

    @Test
    @DisplayName("DELETE /api/v1/journeys/{journeyId}/posts/{journeyPostId}/comments/{commentId} 요청을 service에 위임한다")
    void deleteComment() throws Exception {
        Long journeyId = 1L;
        Long journeyPostId = 101L;
        Long commentId = 11L;

        mockMvc.perform(delete("/api/v1/journeys/{journeyId}/posts/{journeyPostId}/comments/{commentId}", journeyId, journeyPostId, commentId))
                .andExpect(status().isNoContent());

        verify(journeyPostCommentService).deleteComment(journeyId, journeyPostId, commentId, 10L);
    }

    @Test
    @DisplayName("인증되지 않은 사용자는 UNAUTHORIZED 응답을 받는다")
    void unauthenticatedUserReturnsUnauthorized() throws Exception {
        mockMvc = mockMvcWithUser(null);

        mockMvc.perform(get("/api/v1/journeys/{journeyId}/posts/{journeyPostId}/comments", 1L, 101L))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.data.errorCode").value("UNAUTHORIZED"));
    }

    private MockMvc mockMvcWithUser(Long userId) {
        return standaloneSetup(new JourneyPostCommentController(journeyPostCommentService))
                .setCustomArgumentResolvers(new FixedCurrentUserArgumentResolver(userId))
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
