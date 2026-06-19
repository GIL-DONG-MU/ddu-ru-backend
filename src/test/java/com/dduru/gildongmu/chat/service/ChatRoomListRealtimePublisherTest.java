package com.dduru.gildongmu.chat.service;

import com.dduru.gildongmu.chat.constants.ChatDestinationPaths;
import com.dduru.gildongmu.chat.domain.enums.ChatMessageType;
import com.dduru.gildongmu.chat.domain.enums.ChatRoomStatus;
import com.dduru.gildongmu.chat.domain.enums.ChatRoomType;
import com.dduru.gildongmu.chat.dto.query.ChatRoomUserTargetQueryResult;
import com.dduru.gildongmu.chat.dto.response.ChatRoomLastMessageResponse;
import com.dduru.gildongmu.chat.dto.response.ChatRoomListItemResponse;
import com.dduru.gildongmu.chat.dto.ws.roomlist.ChatRoomListEventPayload;
import com.dduru.gildongmu.chat.dto.ws.roomlist.ChatRoomListEventReason;
import com.dduru.gildongmu.chat.dto.ws.roomlist.ChatRoomListEventType;
import com.dduru.gildongmu.chat.exception.ChatRoomNotFoundException;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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
        lenient().when(timeProvider.now()).thenReturn(NOW);
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

    @Test
    @DisplayName("REMOVE 발행 시 채팅방 타입을 찾을 수 없으면 예외를 던지고 발행하지 않는다")
    void publishRoomRemoveToUserWithoutRoomType() {
        Long roomId = 1L;
        Long userId = 10L;

        when(chatRoomRepository.findRoomTypeById(roomId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> publisher.publishRoomRemoveToUser(
                roomId,
                userId,
                ChatRoomListEventReason.MEMBER_CHANGED
        )).isInstanceOf(ChatRoomNotFoundException.class);

        verify(simpMessagingTemplate, never()).convertAndSendToUser(
                eq(String.valueOf(userId)),
                eq(ChatDestinationPaths.USER_CHAT_ROOM_LIST_QUEUE),
                org.mockito.ArgumentMatchers.any(ChatRoomListEventPayload.class)
        );
    }

    @Test
    @DisplayName("게시글 변경 시 연결된 1:1 방과 그룹 방의 현재 멤버에게 메타 갱신을 발행한다")
    void publishRoomMetaUpsertByPostId() {
        Long postId = 100L;
        Long privateRoomId = 1L;
        Long groupRoomId = 2L;
        Long userId = 10L;

        when(chatRoomRepository.findActivePrivateRoomIdsByPostId(postId)).thenReturn(List.of(privateRoomId));
        when(chatRoomRepository.findActiveGroupRoomIdsByJourneyPostId(postId)).thenReturn(List.of(groupRoomId));
        when(chatRoomMemberRepository.findUserIdsByRoomId(privateRoomId)).thenReturn(List.of(userId));
        when(chatRoomMemberRepository.findUserIdsByRoomId(groupRoomId)).thenReturn(List.of(userId));
        when(chatRoomListService.retrieveChatRoomItem(userId, privateRoomId))
                .thenReturn(Optional.of(chatRoomListItem(privateRoomId)));
        when(chatRoomListService.retrieveChatRoomItem(userId, groupRoomId))
                .thenReturn(Optional.of(chatRoomListItem(groupRoomId)));

        publisher.publishRoomMetaUpsertByPostId(postId);

        ArgumentCaptor<ChatRoomListEventPayload> payloadCaptor = ArgumentCaptor.forClass(ChatRoomListEventPayload.class);
        verify(simpMessagingTemplate, times(2)).convertAndSendToUser(
                eq(String.valueOf(userId)),
                eq(ChatDestinationPaths.USER_CHAT_ROOM_LIST_QUEUE),
                payloadCaptor.capture()
        );
        assertThat(payloadCaptor.getAllValues())
                .extracting(payload -> payload.chatRoom().chatRoomId())
                .containsExactlyInAnyOrder(privateRoomId, groupRoomId);
        assertThat(payloadCaptor.getAllValues())
                .extracting(ChatRoomListEventPayload::reason)
                .containsOnly(ChatRoomListEventReason.ROOM_META_UPDATED);
    }

    @Test
    @DisplayName("프로필 변경 시 1:1 방별 갱신 대상 사용자에게만 메타 갱신을 발행한다")
    void publishPrivateRoomMetaUpsertByProfileUserId() {
        Long profileUserId = 20L;
        Long roomId = 1L;
        Long targetUserId1 = 10L;
        Long targetUserId2 = 30L;

        when(chatRoomRepository.findPrivateRoomUpdateTargetsByProfileUserId(profileUserId))
                .thenReturn(List.of(
                        new ChatRoomUserTargetQueryResult(roomId, targetUserId1),
                        new ChatRoomUserTargetQueryResult(roomId, targetUserId2)
                ));
        when(chatRoomListService.retrieveChatRoomItem(targetUserId1, roomId))
                .thenReturn(Optional.of(chatRoomListItem(roomId)));
        when(chatRoomListService.retrieveChatRoomItem(targetUserId2, roomId))
                .thenReturn(Optional.of(chatRoomListItem(roomId)));

        publisher.publishPrivateRoomMetaUpsertByProfileUserId(profileUserId);

        ArgumentCaptor<ChatRoomListEventPayload> payloadCaptor = ArgumentCaptor.forClass(ChatRoomListEventPayload.class);
        verify(simpMessagingTemplate).convertAndSendToUser(
                eq(String.valueOf(targetUserId1)),
                eq(ChatDestinationPaths.USER_CHAT_ROOM_LIST_QUEUE),
                payloadCaptor.capture()
        );
        verify(simpMessagingTemplate).convertAndSendToUser(
                eq(String.valueOf(targetUserId2)),
                eq(ChatDestinationPaths.USER_CHAT_ROOM_LIST_QUEUE),
                payloadCaptor.capture()
        );
        assertThat(payloadCaptor.getAllValues())
                .extracting(ChatRoomListEventPayload::reason)
                .containsOnly(ChatRoomListEventReason.ROOM_META_UPDATED);
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
