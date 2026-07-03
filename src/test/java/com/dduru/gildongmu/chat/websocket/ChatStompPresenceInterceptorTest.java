package com.dduru.gildongmu.chat.websocket;

import com.dduru.gildongmu.chat.repository.ChatRoomMemberRepository;
import com.dduru.gildongmu.chat.service.ChatOnlineStatusService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;

import java.security.Principal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChatStompPresenceInterceptor 테스트")
class ChatStompPresenceInterceptorTest {

    private static final Long ROOM_ID = 10L;
    private static final Long USER_ID = 1L;
    private static final String SESSION_ID = "session-abc";
    private static final String SUBSCRIPTION_ID = "sub-0";
    private static final String CHAT_DESTINATION = "/topic/chat/rooms/" + ROOM_ID;
    private static final String OTHER_DESTINATION = "/topic/notifications";

    @Mock private ChatOnlineStatusService chatOnlineStatusService;
    @Mock private ChatRoomMemberRepository chatRoomMemberRepository;

    @InjectMocks
    private ChatStompPresenceInterceptor interceptor;

    @Nested
    @DisplayName("SUBSCRIBE")
    class Subscribe {

        @Test
        @DisplayName("채팅방 멤버가 구독하면 presence 등록 후 프레임을 통과시킨다")
        void allowsMemberSubscription() {
            when(chatRoomMemberRepository.isMember(ROOM_ID, USER_ID)).thenReturn(true);

            Message<?> result = interceptor.preSend(
                    subscribeMessage(CHAT_DESTINATION, USER_ID),
                    mock(MessageChannel.class)
            );

            assertThat(result).isNotNull();
            verify(chatOnlineStatusService).enter(eq(ROOM_ID), eq(USER_ID), eq(SESSION_ID), eq(SUBSCRIPTION_ID));
        }

        @Test
        @DisplayName("채팅방 비멤버가 구독하면 null을 반환해 프레임을 차단한다")
        void blocksNonMemberSubscription() {
            when(chatRoomMemberRepository.isMember(ROOM_ID, USER_ID)).thenReturn(false);

            Message<?> result = interceptor.preSend(
                    subscribeMessage(CHAT_DESTINATION, USER_ID),
                    mock(MessageChannel.class)
            );

            assertThat(result).isNull();
            verify(chatOnlineStatusService, never()).enter(any(), any(), any(), any());
        }

        @Test
        @DisplayName("인증 정보 없이 채팅방 구독 시도하면 null을 반환해 프레임을 차단한다")
        void blocksUnauthenticatedSubscription() {
            Message<?> result = interceptor.preSend(
                    subscribeMessage(CHAT_DESTINATION, null),
                    mock(MessageChannel.class)
            );

            assertThat(result).isNull();
            verify(chatRoomMemberRepository, never()).isMember(any(), any());
            verify(chatOnlineStatusService, never()).enter(any(), any(), any(), any());
        }

        @Test
        @DisplayName("채팅방이 아닌 destination 구독은 멤버 검증 없이 통과시킨다")
        void allowsNonChatSubscription() {
            Message<?> result = interceptor.preSend(
                    subscribeMessage(OTHER_DESTINATION, USER_ID),
                    mock(MessageChannel.class)
            );

            assertThat(result).isNotNull();
            verify(chatRoomMemberRepository, never()).isMember(any(), any());
            verify(chatOnlineStatusService, never()).enter(any(), any(), any(), any());
        }
    }

    @Nested
    @DisplayName("UNSUBSCRIBE")
    class Unsubscribe {

        @Test
        @DisplayName("UNSUBSCRIBE 프레임은 presence 해제 후 통과시킨다")
        void leavesOnUnsubscribe() {
            Message<?> result = interceptor.preSend(
                    unsubscribeMessage(USER_ID),
                    mock(MessageChannel.class)
            );

            assertThat(result).isNotNull();
            verify(chatOnlineStatusService).leave(SESSION_ID, SUBSCRIPTION_ID, USER_ID);
        }
    }

    // ── 헬퍼 메서드 ──────────────────────────────────────────────────────────

    private static Message<byte[]> subscribeMessage(String destination, Long userId) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        accessor.setDestination(destination);
        accessor.setSubscriptionId(SUBSCRIPTION_ID);
        accessor.setSessionId(SESSION_ID);
        if (userId != null) {
            accessor.setUser(principal(userId));
        }
        accessor.setLeaveMutable(true);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }

    private static Message<byte[]> unsubscribeMessage(Long userId) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.UNSUBSCRIBE);
        accessor.setSubscriptionId(SUBSCRIPTION_ID);
        accessor.setSessionId(SESSION_ID);
        accessor.setUser(principal(userId));
        accessor.setLeaveMutable(true);
        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }

    private static Principal principal(Long userId) {
        return () -> String.valueOf(userId);
    }
}
