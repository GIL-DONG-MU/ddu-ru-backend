package com.dduru.gildongmu.chat.controller;

import com.dduru.gildongmu.auth.exception.UnauthorizedException;
import com.dduru.gildongmu.chat.domain.enums.ChatMessageType;
import com.dduru.gildongmu.chat.domain.enums.ChatRoomType;
import com.dduru.gildongmu.chat.dto.request.ChatMessageRetrieveRequest;
import com.dduru.gildongmu.chat.dto.request.ChatReadRequest;
import com.dduru.gildongmu.chat.dto.response.ChatMessageItemResponse;
import com.dduru.gildongmu.chat.dto.response.ChatMessagePageResponse;
import com.dduru.gildongmu.chat.dto.response.ChatMessageSenderResponse;
import com.dduru.gildongmu.chat.dto.response.ChatMessagesResponse;
import com.dduru.gildongmu.chat.dto.response.ChatReadResponse;
import com.dduru.gildongmu.chat.dto.response.ChatRoomInfoResponse;
import com.dduru.gildongmu.chat.service.ChatMessageQueryService;
import com.dduru.gildongmu.chat.service.ChatReadService;
import com.dduru.gildongmu.chat.service.PrivateChatRoomService;
import com.dduru.gildongmu.common.annotation.CurrentUser;
import com.dduru.gildongmu.common.exception.GlobalExceptionHandler;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChatController 테스트")
class ChatControllerTest {

    @Mock
    private PrivateChatRoomService privateChatRoomService;

    @Mock
    private ChatMessageQueryService chatMessageQueryService;

    @Mock
    private ChatReadService chatReadService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = mockMvcWithUser(10L);
    }

    @Test
    @DisplayName("GET /api/v1/chat-rooms/{chatRoomId}/messages 요청을 service에 위임하고 응답한다")
    void retrieveMessages() throws Exception {
        Long roomId = 1L;
        ChatMessagesResponse response = new ChatMessagesResponse(
                new ChatRoomInfoResponse(roomId, ChatRoomType.PRIVATE, true, "코끼리 아저씨", "guestNick", null, null),
                new ChatMessagePageResponse(20, false, null),
                List.of(new ChatMessageItemResponse(
                        101L,
                        ChatMessageType.TEXT,
                        new ChatMessageSenderResponse(20L, "guestNick", false),
                        false,
                        "안녕하세요",
                        List.of(),
                        null,
                        null,
                        LocalDateTime.of(2026, 5, 9, 9, 41)
                ))
        );
        when(chatMessageQueryService.retrieveMessages(eq(10L), eq(roomId), any(ChatMessageRetrieveRequest.class)))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/chat-rooms/{chatRoomId}/messages", roomId)
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.roomInfo.chatRoomId").value(1))
                .andExpect(jsonPath("$.data.roomInfo.isActive").value(true))
                .andExpect(jsonPath("$.data.roomInfo.opponentNickname").value("guestNick"))
                .andExpect(jsonPath("$.data.page.size").value(20))
                .andExpect(jsonPath("$.data.page.hasNext").value(false))
                .andExpect(jsonPath("$.data.messages[0].messageId").value(101))
                .andExpect(jsonPath("$.data.messages[0].sender.nickname").value("guestNick"))
                .andExpect(jsonPath("$.data.messages[0].isMine").value(false));

        ArgumentCaptor<ChatMessageRetrieveRequest> requestCaptor = ArgumentCaptor.forClass(ChatMessageRetrieveRequest.class);
        org.mockito.Mockito.verify(chatMessageQueryService).retrieveMessages(eq(10L), eq(roomId), requestCaptor.capture());
        assertThat(requestCaptor.getValue().beforeMessageId()).isNull();
        assertThat(requestCaptor.getValue().size()).isEqualTo(20);
    }

    @Test
    @DisplayName("인증되지 않은 사용자는 UNAUTHORIZED 응답을 받는다")
    void unauthenticatedUserReturnsUnauthorized() throws Exception {
        mockMvc = mockMvcWithUser(null);

        mockMvc.perform(get("/api/v1/chat-rooms/{chatRoomId}/messages", 1L))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.data.errorCode").value("UNAUTHORIZED"));
    }

    @Test
    @DisplayName("잘못된 query 값은 INVALID_INPUT_VALUE 응답을 받는다")
    void invalidQueryReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/chat-rooms/{chatRoomId}/messages", 1L)
                        .param("size", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.errorCode").value("INVALID_INPUT_VALUE"))
                .andExpect(jsonPath("$.data.field").value("size"));

        org.mockito.Mockito.verifyNoInteractions(chatMessageQueryService);
    }

    @Test
    @DisplayName("PATCH /api/v1/chat-rooms/{chatRoomId}/read 요청을 service에 위임하고 응답한다")
    void readMessages() throws Exception {
        Long roomId = 1L;
        when(chatReadService.read(eq(10L), eq(roomId), any(ChatReadRequest.class)))
                .thenReturn(new ChatReadResponse(roomId, 123L, true));

        mockMvc.perform(patch("/api/v1/chat-rooms/{chatRoomId}/read", roomId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"lastReadMessageId":123}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.chatRoomId").value(1))
                .andExpect(jsonPath("$.data.lastReadMessageId").value(123))
                .andExpect(jsonPath("$.data.updated").value(true));

        ArgumentCaptor<ChatReadRequest> requestCaptor = ArgumentCaptor.forClass(ChatReadRequest.class);
        org.mockito.Mockito.verify(chatReadService).read(eq(10L), eq(roomId), requestCaptor.capture());
        assertThat(requestCaptor.getValue().lastReadMessageId()).isEqualTo(123L);
    }

    @Test
    @DisplayName("읽음 처리 인증되지 않은 사용자는 UNAUTHORIZED 응답을 받는다")
    void readMessagesUnauthenticatedUserReturnsUnauthorized() throws Exception {
        mockMvc = mockMvcWithUser(null);

        mockMvc.perform(patch("/api/v1/chat-rooms/{chatRoomId}/read", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"lastReadMessageId":123}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.data.errorCode").value("UNAUTHORIZED"));
    }

    @Test
    @DisplayName("lastReadMessageId가 없거나 양수가 아니면 INVALID_INPUT_VALUE 응답을 받는다")
    void readMessagesInvalidRequestReturnsBadRequest() throws Exception {
        mockMvc.perform(patch("/api/v1/chat-rooms/{chatRoomId}/read", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.errorCode").value("INVALID_INPUT_VALUE"))
                .andExpect(jsonPath("$.data.field").value("lastReadMessageId"));

        mockMvc.perform(patch("/api/v1/chat-rooms/{chatRoomId}/read", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"lastReadMessageId":0}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.errorCode").value("INVALID_INPUT_VALUE"))
                .andExpect(jsonPath("$.data.field").value("lastReadMessageId"));

        org.mockito.Mockito.verifyNoInteractions(chatReadService);
    }

    @Test
    @DisplayName("query type mismatch도 INVALID_INPUT_VALUE 응답을 받는다")
    void invalidQueryTypeReturnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/v1/chat-rooms/{chatRoomId}/messages", 1L)
                        .param("size", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.errorCode").value("INVALID_INPUT_VALUE"))
                .andExpect(jsonPath("$.data.field").value("size"));

        org.mockito.Mockito.verifyNoInteractions(chatMessageQueryService);
    }

    private MockMvc mockMvcWithUser(Long userId) {
        return standaloneSetup(new ChatController(privateChatRoomService, chatMessageQueryService, chatReadService))
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
