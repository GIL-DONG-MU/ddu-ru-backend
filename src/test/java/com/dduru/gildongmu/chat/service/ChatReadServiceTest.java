package com.dduru.gildongmu.chat.service;

import com.dduru.gildongmu.chat.constants.ChatDestinationPaths;
import com.dduru.gildongmu.chat.domain.ChatMessage;
import com.dduru.gildongmu.chat.domain.ChatRoom;
import com.dduru.gildongmu.chat.domain.ChatRoomMember;
import com.dduru.gildongmu.chat.domain.enums.ChatMemberRole;
import com.dduru.gildongmu.chat.domain.enums.ChatMessageType;
import com.dduru.gildongmu.chat.domain.enums.ChatRoomStatus;
import com.dduru.gildongmu.chat.dto.request.ChatReadRequest;
import com.dduru.gildongmu.chat.dto.response.ChatReadResponse;
import com.dduru.gildongmu.chat.dto.ws.ChatReadEventPayload;
import com.dduru.gildongmu.chat.event.ChatReadUpdatedEvent;
import com.dduru.gildongmu.chat.exception.ChatAccessDeniedException;
import com.dduru.gildongmu.chat.exception.ChatMessageNotFoundException;
import com.dduru.gildongmu.chat.exception.ChatRoomNotFoundException;
import com.dduru.gildongmu.chat.repository.ChatMessageRepository;
import com.dduru.gildongmu.chat.repository.ChatRoomMemberRepository;
import com.dduru.gildongmu.chat.repository.ChatRoomRepository;
import com.dduru.gildongmu.common.time.TimeProvider;
import com.dduru.gildongmu.journey.domain.Journey;
import com.dduru.gildongmu.journey.domain.enums.JourneyMemberStatus;
import com.dduru.gildongmu.journey.repository.JourneyMemberRepository;
import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.profile.domain.enums.Gender;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.domain.enums.OauthType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChatReadService 테스트")
class ChatReadServiceTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 5, 11, 21, 35);

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private ChatRoomMemberRepository chatRoomMemberRepository;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private JourneyMemberRepository journeyMemberRepository;

    @Mock
    private SimpMessagingTemplate simpMessagingTemplate;

    @Mock
    private TimeProvider timeProvider;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private ChatReadService chatReadService;

    @BeforeEach
    void setUp() {
        lenient().when(timeProvider.now()).thenReturn(NOW);
        chatReadService = new ChatReadService(
                chatRoomRepository,
                chatRoomMemberRepository,
                chatMessageRepository,
                journeyMemberRepository,
                simpMessagingTemplate,
                timeProvider,
                eventPublisher
        );
    }

    @Nested
    @DisplayName("읽음 처리 성공")
    class ReadSuccess {

        @Test
        @DisplayName("현재 멤버가 최신 메시지 ID로 읽음 처리하면 lastReadMessage가 갱신되고 READ 이벤트가 발행된다")
        void updatesLastReadMessageAndPublishesReadEvent() {
            Long roomId = 1L;
            Long userId = 10L;
            User user = createUser(userId, "reader");
            ChatRoom room = createPrivateRoom(roomId, createPost(100L, user), ChatRoomStatus.ACTIVE);
            ChatRoomMember member = createMember(room, user, null, LocalDateTime.of(2026, 5, 9, 9, 0));
            ChatMessage message = createMessage(123L, room, user, ChatMessageType.TEXT,
                    "읽음 처리할 메시지", LocalDateTime.of(2026, 5, 9, 9, 10));

            when(chatRoomRepository.getByIdWithContextOrThrow(roomId)).thenReturn(room);
            when(chatRoomMemberRepository.findByRoomIdAndUserIdWithLock(roomId, userId)).thenReturn(Optional.of(member));
            when(chatMessageRepository.findByIdAndRoomId(message.getId(), roomId)).thenReturn(Optional.of(message));

            ChatReadResponse response = chatReadService.read(userId, roomId, new ChatReadRequest(message.getId()));

            assertThat(response.chatRoomId()).isEqualTo(roomId);
            assertThat(response.lastReadMessageId()).isEqualTo(message.getId());
            assertThat(response.updated()).isTrue();
            assertThat(member.getLastReadMessage()).isEqualTo(message);
            verify(eventPublisher).publishEvent(new ChatReadUpdatedEvent(roomId, userId));

            ArgumentCaptor<ChatReadEventPayload> payloadCaptor = ArgumentCaptor.forClass(ChatReadEventPayload.class);
            verify(simpMessagingTemplate).convertAndSend(
                    org.mockito.ArgumentMatchers.eq(ChatDestinationPaths.topicRoom(roomId)),
                    payloadCaptor.capture()
            );
            assertThat(payloadCaptor.getValue().eventType()).isEqualTo("READ");
            assertThat(payloadCaptor.getValue().roomId()).isEqualTo(roomId);
            assertThat(payloadCaptor.getValue().readerUserId()).isEqualTo(userId);
            assertThat(payloadCaptor.getValue().lastReadMessageId()).isEqualTo(message.getId());
            assertThat(payloadCaptor.getValue().readAt()).isEqualTo(NOW);
        }

        @Test
        @DisplayName("트랜잭션 동기화가 활성화되어 있으면 READ 이벤트는 afterCommit에서 발행된다")
        void publishesReadEventAfterCommitWhenTransactionSynchronizationActive() {
            Long roomId = 1L;
            Long userId = 10L;
            User user = createUser(userId, "reader");
            ChatRoom room = createPrivateRoom(roomId, createPost(100L, user), ChatRoomStatus.ACTIVE);
            ChatRoomMember member = createMember(room, user, null, LocalDateTime.of(2026, 5, 9, 9, 0));
            ChatMessage message = createMessage(123L, room, user, ChatMessageType.TEXT,
                    "읽음 처리할 메시지", LocalDateTime.of(2026, 5, 9, 9, 10));

            when(chatRoomRepository.getByIdWithContextOrThrow(roomId)).thenReturn(room);
            when(chatRoomMemberRepository.findByRoomIdAndUserIdWithLock(roomId, userId)).thenReturn(Optional.of(member));
            when(chatMessageRepository.findByIdAndRoomId(message.getId(), roomId)).thenReturn(Optional.of(message));

            TransactionSynchronizationManager.initSynchronization();
            try {
                chatReadService.read(userId, roomId, new ChatReadRequest(message.getId()));

                verifyNoInteractions(simpMessagingTemplate);
                List<TransactionSynchronization> synchronizations = TransactionSynchronizationManager.getSynchronizations();
                assertThat(synchronizations).hasSize(1);

                synchronizations.get(0).afterCommit();

                verify(simpMessagingTemplate).convertAndSend(
                        org.mockito.ArgumentMatchers.eq(ChatDestinationPaths.topicRoom(roomId)),
                        any(ChatReadEventPayload.class)
                );
            } finally {
                TransactionSynchronizationManager.clearSynchronization();
            }
        }

        @Test
        @DisplayName("기존 읽음 위치보다 과거 메시지를 요청하면 updated=false이고 READ 이벤트를 발행하지 않는다")
        void oldMessageReturnsNoOp() {
            Long roomId = 1L;
            Long userId = 10L;
            User user = createUser(userId, "reader");
            ChatRoom room = createPrivateRoom(roomId, createPost(100L, user), ChatRoomStatus.ACTIVE);
            ChatMessage oldMessage = createMessage(150L, room, user, ChatMessageType.TEXT,
                    "과거 메시지", LocalDateTime.of(2026, 5, 9, 9, 10));
            ChatMessage currentLastRead = createMessage(200L, room, user, ChatMessageType.TEXT,
                    "이미 읽은 메시지", LocalDateTime.of(2026, 5, 9, 9, 20));
            ChatRoomMember member = createMember(room, user, currentLastRead, LocalDateTime.of(2026, 5, 9, 9, 0));

            when(chatRoomRepository.getByIdWithContextOrThrow(roomId)).thenReturn(room);
            when(chatRoomMemberRepository.findByRoomIdAndUserIdWithLock(roomId, userId)).thenReturn(Optional.of(member));
            when(chatMessageRepository.findByIdAndRoomId(oldMessage.getId(), roomId)).thenReturn(Optional.of(oldMessage));

            ChatReadResponse response = chatReadService.read(userId, roomId, new ChatReadRequest(oldMessage.getId()));

            assertThat(response.lastReadMessageId()).isEqualTo(currentLastRead.getId());
            assertThat(response.updated()).isFalse();
            assertThat(member.getLastReadMessage()).isEqualTo(currentLastRead);
            verifyNoInteractions(simpMessagingTemplate);
            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("기존 읽음 위치와 같은 메시지를 요청해도 updated=false이고 READ 이벤트를 발행하지 않는다")
        void sameMessageReturnsNoOp() {
            Long roomId = 1L;
            Long userId = 10L;
            User user = createUser(userId, "reader");
            ChatRoom room = createPrivateRoom(roomId, createPost(100L, user), ChatRoomStatus.ACTIVE);
            ChatMessage currentLastRead = createMessage(200L, room, user, ChatMessageType.TEXT,
                    "이미 읽은 메시지", LocalDateTime.of(2026, 5, 9, 9, 20));
            ChatRoomMember member = createMember(room, user, currentLastRead, LocalDateTime.of(2026, 5, 9, 9, 0));

            when(chatRoomRepository.getByIdWithContextOrThrow(roomId)).thenReturn(room);
            when(chatRoomMemberRepository.findByRoomIdAndUserIdWithLock(roomId, userId)).thenReturn(Optional.of(member));
            when(chatMessageRepository.findByIdAndRoomId(currentLastRead.getId(), roomId))
                    .thenReturn(Optional.of(currentLastRead));

            ChatReadResponse response = chatReadService.read(
                    userId,
                    roomId,
                    new ChatReadRequest(currentLastRead.getId())
            );

            assertThat(response.lastReadMessageId()).isEqualTo(currentLastRead.getId());
            assertThat(response.updated()).isFalse();
            verifyNoInteractions(simpMessagingTemplate);
            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("SYSTEM 메시지와 CLOSED 방도 읽음 처리할 수 있다")
        void systemMessageInClosedRoomCanBeRead() {
            Long roomId = 1L;
            Long userId = 10L;
            User user = createUser(userId, "reader");
            ChatRoom room = createPrivateRoom(roomId, createPost(100L, user), ChatRoomStatus.CLOSED);
            ChatRoomMember member = createMember(room, user, null, LocalDateTime.of(2026, 5, 9, 9, 0));
            ChatMessage message = createMessage(123L, room, null, ChatMessageType.SYSTEM,
                    "{}", LocalDateTime.of(2026, 5, 9, 9, 10));

            when(chatRoomRepository.getByIdWithContextOrThrow(roomId)).thenReturn(room);
            when(chatRoomMemberRepository.findByRoomIdAndUserIdWithLock(roomId, userId)).thenReturn(Optional.of(member));
            when(chatMessageRepository.findByIdAndRoomId(message.getId(), roomId)).thenReturn(Optional.of(message));

            ChatReadResponse response = chatReadService.read(userId, roomId, new ChatReadRequest(message.getId()));

            assertThat(response.updated()).isTrue();
            assertThat(member.getLastReadMessage()).isEqualTo(message);
        }
    }

    @Nested
    @DisplayName("읽음 처리 실패")
    class ReadFailure {

        @Test
        @DisplayName("DELETED 방은 찾을 수 없는 방으로 처리한다")
        void deletedRoomThrowsNotFound() {
            Long roomId = 1L;
            User user = createUser(10L, "reader");
            ChatRoom room = createPrivateRoom(roomId, createPost(100L, user), ChatRoomStatus.DELETED);

            when(chatRoomRepository.getByIdWithContextOrThrow(roomId)).thenReturn(room);

            assertThatThrownBy(() -> chatReadService.read(user.getId(), roomId, new ChatReadRequest(123L)))
                    .isInstanceOf(ChatRoomNotFoundException.class);

            verifyNoInteractions(chatRoomMemberRepository, chatMessageRepository, simpMessagingTemplate);
        }

        @Test
        @DisplayName("채팅방 멤버가 아니면 접근 예외가 발생한다")
        void nonMemberThrowsAccessDenied() {
            Long roomId = 1L;
            Long userId = 10L;
            User user = createUser(userId, "reader");
            ChatRoom room = createPrivateRoom(roomId, createPost(100L, user), ChatRoomStatus.ACTIVE);

            when(chatRoomRepository.getByIdWithContextOrThrow(roomId)).thenReturn(room);
            when(chatRoomMemberRepository.findByRoomIdAndUserIdWithLock(roomId, userId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> chatReadService.read(userId, roomId, new ChatReadRequest(123L)))
                    .isInstanceOf(ChatAccessDeniedException.class);

            verifyNoInteractions(chatMessageRepository, simpMessagingTemplate);
        }

        @Test
        @DisplayName("그룹방의 ACTIVE journey member가 아니면 접근 예외가 발생한다")
        void inactiveJourneyMemberThrowsAccessDenied() {
            Long roomId = 1L;
            Long userId = 10L;
            User user = createUser(userId, "reader");
            Journey journey = createJourney(30L, createPost(100L, user));
            ChatRoom room = createGroupRoom(roomId, journey, ChatRoomStatus.ACTIVE);
            ChatRoomMember member = createMember(room, user, null, LocalDateTime.of(2026, 5, 9, 9, 0));

            when(chatRoomRepository.getByIdWithContextOrThrow(roomId)).thenReturn(room);
            when(chatRoomMemberRepository.findByRoomIdAndUserIdWithLock(roomId, userId)).thenReturn(Optional.of(member));
            when(journeyMemberRepository.existsByJourneyIdAndUserIdAndStatus(30L, userId, JourneyMemberStatus.ACTIVE))
                    .thenReturn(false);

            assertThatThrownBy(() -> chatReadService.read(userId, roomId, new ChatReadRequest(123L)))
                    .isInstanceOf(ChatAccessDeniedException.class);

            verifyNoInteractions(chatMessageRepository, simpMessagingTemplate);
        }

        @Test
        @DisplayName("다른 방 메시지 ID로 요청하면 메시지 not found 예외가 발생한다")
        void messageFromOtherRoomThrowsNotFound() {
            Long roomId = 1L;
            Long userId = 10L;
            User user = createUser(userId, "reader");
            ChatRoom room = createPrivateRoom(roomId, createPost(100L, user), ChatRoomStatus.ACTIVE);
            ChatRoomMember member = createMember(room, user, null, LocalDateTime.of(2026, 5, 9, 9, 0));

            when(chatRoomRepository.getByIdWithContextOrThrow(roomId)).thenReturn(room);
            when(chatRoomMemberRepository.findByRoomIdAndUserIdWithLock(roomId, userId)).thenReturn(Optional.of(member));
            when(chatMessageRepository.findByIdAndRoomId(999L, roomId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> chatReadService.read(userId, roomId, new ChatReadRequest(999L)))
                    .isInstanceOf(ChatMessageNotFoundException.class);

            verifyNoInteractions(simpMessagingTemplate);
        }

        @Test
        @DisplayName("현재 사용자의 참여 시점 이전 메시지는 읽음 처리할 수 없다")
        void messageBeforeVisibleFromThrowsAccessDenied() {
            Long roomId = 1L;
            Long userId = 10L;
            User user = createUser(userId, "reader");
            ChatRoom room = createPrivateRoom(roomId, createPost(100L, user), ChatRoomStatus.ACTIVE);
            ChatRoomMember member = createMember(room, user, null, LocalDateTime.of(2026, 5, 9, 9, 10));
            ChatMessage message = createMessage(123L, room, user, ChatMessageType.TEXT,
                    "참여 전 메시지", LocalDateTime.of(2026, 5, 9, 9, 9));

            when(chatRoomRepository.getByIdWithContextOrThrow(roomId)).thenReturn(room);
            when(chatRoomMemberRepository.findByRoomIdAndUserIdWithLock(roomId, userId)).thenReturn(Optional.of(member));
            when(chatMessageRepository.findByIdAndRoomId(message.getId(), roomId)).thenReturn(Optional.of(message));

            assertThatThrownBy(() -> chatReadService.read(userId, roomId, new ChatReadRequest(message.getId())))
                    .isInstanceOf(ChatAccessDeniedException.class);

            verify(simpMessagingTemplate, never()).convertAndSend(any(String.class), any(Object.class));
        }
    }

    private ChatRoom createPrivateRoom(Long roomId, Post post, ChatRoomStatus status) {
        ChatRoom room = ChatRoom.forPrivateChat(post);
        ReflectionTestUtils.setField(room, "id", roomId);
        ReflectionTestUtils.setField(room, "status", status);
        return room;
    }

    private ChatRoom createGroupRoom(Long roomId, Journey journey, ChatRoomStatus status) {
        ChatRoom room = ChatRoom.createGroupChat(journey);
        ReflectionTestUtils.setField(room, "id", roomId);
        ReflectionTestUtils.setField(room, "status", status);
        return room;
    }

    private ChatRoomMember createMember(
            ChatRoom room,
            User user,
            ChatMessage lastReadMessage,
            LocalDateTime createdAt
    ) {
        ChatRoomMember member = ChatRoomMember.builder()
                .room(room)
                .user(user)
                .role(ChatMemberRole.GUEST)
                .lastReadMessage(lastReadMessage)
                .build();
        ReflectionTestUtils.setField(member, "createdAt", createdAt);
        return member;
    }

    private ChatMessage createMessage(
            Long messageId,
            ChatRoom room,
            User sender,
            ChatMessageType messageType,
            String content,
            LocalDateTime createdAt
    ) {
        ChatMessage message = ChatMessage.create(room, sender, messageType, content);
        ReflectionTestUtils.setField(message, "id", messageId);
        ReflectionTestUtils.setField(message, "createdAt", createdAt);
        return message;
    }

    private Journey createJourney(Long journeyId, Post post) {
        Journey journey = Journey.create(post);
        ReflectionTestUtils.setField(journey, "id", journeyId);
        ReflectionTestUtils.setField(journey, "title", "그룹 여정");
        return journey;
    }

    private Post createPost(Long postId, User owner) {
        Post post = Post.createPost(
                owner,
                null,
                "채팅 읽음 테스트 게시글",
                "채팅 읽음 테스트용 본문입니다.",
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(2),
                4,
                LocalDate.now().plusDays(1),
                Gender.U,
                true,
                null,
                null,
                "https://example.com/photo.png",
                "[]",
                null
        );
        ReflectionTestUtils.setField(post, "id", postId);
        return post;
    }

    private User createUser(Long userId, String name) {
        User user = User.builder()
                .email(name + userId + "@example.com")
                .name(name)
                .oauthId("oauth-" + userId)
                .oauthType(OauthType.KAKAO)
                .build();
        ReflectionTestUtils.setField(user, "id", userId);
        return user;
    }
}
