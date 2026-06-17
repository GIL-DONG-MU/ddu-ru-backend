package com.dduru.gildongmu.chat.service;

import com.dduru.gildongmu.chat.constants.ChatDestinationPaths;
import com.dduru.gildongmu.chat.domain.enums.ChatMessageType;
import com.dduru.gildongmu.chat.domain.enums.ChatRoomStatus;
import com.dduru.gildongmu.chat.domain.enums.ChatRoomType;
import com.dduru.gildongmu.chat.dto.response.ChatRoomLastMessageResponse;
import com.dduru.gildongmu.chat.dto.response.ChatRoomListItemResponse;
import com.dduru.gildongmu.chat.dto.ws.roomlist.ChatRoomListEventPayload;
import com.dduru.gildongmu.chat.dto.ws.roomlist.ChatRoomListEventReason;
import com.dduru.gildongmu.chat.dto.ws.roomlist.ChatRoomListEventType;
import com.dduru.gildongmu.chat.repository.ChatRoomMemberRepository;
import com.dduru.gildongmu.chat.repository.ChatRoomRepository;
import com.dduru.gildongmu.common.time.TimeProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChatRoomListRealtimePublisher 테스트")
class ChatRoomListRealtimePublisherTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 6, 13, 14, 31);

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private ChatRoomMemberRepository chatRoomMemberRepository;

    @Mock
    private ChatRoomListService chatRoomListService;

    @Mock
    private SimpMessagingTemplate simpMessagingTemplate;

    @Mock
    private TimeProvider timeProvider;

    private ChatRoomListRealtimePublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new ChatRoomListRealtimePublisher(
                chatRoomRepository,
                chatRoomMemberRepository,
                chatRoomListService,
                simpMessagingTemplate,
                timeProvider
        );
        when(timeProvider.now()).thenReturn(NOW);
    }

    @Test
    @DisplayName("현재 채팅방 멤버에게 사용자별 완성된 UPSERT payload를 발행한다")
    void publishRoomUpsertToCurrentMembers() {
        Long roomId = 1L;
        Long userId = 10L;
        ChatRoomListItemResponse item = chatRoomListItem(roomId);

        when(chatRoomMemberRepository.findUserIdsByRoomId(roomId)).thenReturn(List.of(userId));
        when(chatRoomListService.retrieveChatRoomItem(userId, roomId)).thenReturn(Optional.of(item));

        publisher.publishRoomUpsertToCurrentMembers(roomId, ChatRoomListEventReason.MESSAGE_CREATED);

        ArgumentCaptor<ChatRoomListEventPayload> payloadCaptor = ArgumentCaptor.forClass(ChatRoomListEventPayload.class);
        verify(simpMessagingTemplate).convertAndSendToUser(
                eq(String.valueOf(userId)),
                eq(ChatDestinationPaths.USER_CHAT_ROOM_LIST_QUEUE),
                payloadCaptor.capture()
        );
        ChatRoomListEventPayload payload = payloadCaptor.getValue();
        assertThat(payload.eventType()).isEqualTo(ChatRoomListEventType.UPSERT);
        assertThat(payload.reason()).isEqualTo(ChatRoomListEventReason.MESSAGE_CREATED);
        assertThat(payload.chatRoom().chatRoomId()).isEqualTo(roomId);
        assertThat(payload.chatRoom().lastMessage().content()).isEqualTo("안녕하세요");
        assertThat(payload.chatRoom().activityAt()).isEqualTo(LocalDateTime.of(2026, 6, 13, 14, 30));
        assertThat(payload.occurredAt()).isEqualTo(NOW);
    }

    @Test
    @DisplayName("REMOVE payload는 최소 room 정보로 개인 queue에 발행한다")
    void publishRoomRemoveToUser() {
        Long roomId = 1L;
        Long userId = 10L;

        when(chatRoomRepository.findRoomTypeById(roomId)).thenReturn(Optional.of(ChatRoomType.GROUP));

        publisher.publishRoomRemoveToUser(roomId, userId, ChatRoomListEventReason.MEMBER_CHANGED);

        ArgumentCaptor<ChatRoomListEventPayload> payloadCaptor = ArgumentCaptor.forClass(ChatRoomListEventPayload.class);
        verify(simpMessagingTemplate).convertAndSendToUser(
                eq(String.valueOf(userId)),
                eq(ChatDestinationPaths.USER_CHAT_ROOM_LIST_QUEUE),
                payloadCaptor.capture()
        );
        ChatRoomListEventPayload payload = payloadCaptor.getValue();
        assertThat(payload.eventType()).isEqualTo(ChatRoomListEventType.REMOVE);
        assertThat(payload.reason()).isEqualTo(ChatRoomListEventReason.MEMBER_CHANGED);
        assertThat(payload.chatRoom().chatRoomId()).isEqualTo(roomId);
        assertThat(payload.chatRoom().roomType()).isEqualTo(ChatRoomType.GROUP);
        assertThat(payload.chatRoom().displayName()).isNull();
        assertThat(payload.occurredAt()).isEqualTo(NOW);
    }

    private static ChatRoomListItemResponse chatRoomListItem(Long roomId) {
        return new ChatRoomListItemResponse(
                roomId,
                ChatRoomType.PRIVATE,
                ChatRoomStatus.ACTIVE,
                "guestNick",
                "제주 애월 2박 3일",
                "https://example.com/profile.png",
                100L,
                null,
                2,
                new ChatRoomLastMessageResponse(
                        1234L,
                        ChatMessageType.TEXT,
                        "안녕하세요",
                        20L,
                        "guestNick",
                        LocalDateTime.of(2026, 6, 13, 14, 30)
                ),
                3L,
                1200L,
                LocalDateTime.of(2026, 6, 13, 14, 30),
                LocalDateTime.of(2026, 6, 1, 10, 0)
        );
    }
}
