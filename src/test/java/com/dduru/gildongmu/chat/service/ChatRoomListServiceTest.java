package com.dduru.gildongmu.chat.service;

import com.dduru.gildongmu.chat.cursor.ChatRoomListCursorCodec;
import com.dduru.gildongmu.chat.domain.ChatMessage;
import com.dduru.gildongmu.chat.domain.ChatRoom;
import com.dduru.gildongmu.chat.domain.ChatRoomMember;
import com.dduru.gildongmu.chat.domain.enums.ChatMemberRole;
import com.dduru.gildongmu.chat.domain.enums.ChatMessageType;
import com.dduru.gildongmu.chat.domain.enums.ChatRoomType;
import com.dduru.gildongmu.chat.dto.query.ChatRoomListCursor;
import com.dduru.gildongmu.chat.dto.query.ChatRoomListQueryResult;
import com.dduru.gildongmu.chat.dto.request.ChatRoomListRequest;
import com.dduru.gildongmu.chat.dto.request.ChatRoomListType;
import com.dduru.gildongmu.chat.dto.response.ChatRoomListResponse;
import com.dduru.gildongmu.chat.repository.ChatRoomMemberRepository;
import com.dduru.gildongmu.chat.repository.ChatRoomRepository;
import com.dduru.gildongmu.destination.domain.Destination;
import com.dduru.gildongmu.journey.domain.Journey;
import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.profile.domain.Profile;
import com.dduru.gildongmu.profile.domain.enums.Gender;
import com.dduru.gildongmu.profile.utils.ProfileImageResolver;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.domain.enums.OauthType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChatRoomListService 테스트")
class ChatRoomListServiceTest {

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private ChatRoomMemberRepository chatRoomMemberRepository;

    @Mock
    private ProfileImageResolver profileImageResolver;

    @Mock
    private ChatRoomListCursorCodec cursorCodec;

    private ChatRoomListService service;

    @BeforeEach
    void setUp() {
        service = new ChatRoomListService(
                chatRoomRepository,
                chatRoomMemberRepository,
                profileImageResolver,
                cursorCodec
        );
    }

    @Test
    @DisplayName("채팅방 목록 표시 정보, 마지막 메시지, unread count, next cursor를 조립한다")
    void retrieveChatRooms() {
        Long currentUserId = 10L;
        User currentUser = createUser(currentUserId, "host", "hostNick");
        User opponent = createUser(20L, "guest", "guestNick");
        User invitee = createUser(30L, "invitee", "inviteeNick");
        Destination destination = createDestination();
        Post privatePost = createPost(100L, currentUser, destination, "제주 애월 2박 3일", "https://post/private.png");
        Post groupPost = createPost(101L, currentUser, destination, "제주 모집글", "https://post/group.png");
        Journey journey = createJourney(300L, groupPost, "제주 그룹 여정", null);

        ChatRoom privateRoom = createPrivateRoom(1L, privatePost, LocalDateTime.of(2026, 6, 1, 10, 0));
        ChatRoom groupRoom = createGroupRoom(2L, journey, LocalDateTime.of(2026, 6, 2, 10, 0));
        ChatMessage privateReadCursor = createMessage(
                90L,
                privateRoom,
                currentUser,
                ChatMessageType.TEXT,
                "읽은 메시지",
                LocalDateTime.of(2026, 6, 13, 13, 0)
        );
        ChatRoomMember privateCurrentMember = createMember(privateRoom, currentUser, privateReadCursor);
        ChatRoomMember privateOpponentMember = createMember(privateRoom, opponent, null);
        ChatRoomMember groupCurrentMember = createMember(groupRoom, currentUser, null);
        ChatRoomMember groupInviteeMember = createMember(groupRoom, invitee, null);
        ChatMessage privateLastMessage = createMessage(
                100L,
                privateRoom,
                opponent,
                ChatMessageType.TEXT,
                "안녕하세요",
                LocalDateTime.of(2026, 6, 13, 14, 30)
        );
        ChatMessage groupLastMessage = createMessage(
                200L,
                groupRoom,
                invitee,
                ChatMessageType.TEXT,
                "그룹 안녕하세요",
                LocalDateTime.of(2026, 6, 13, 15, 0)
        );

        when(cursorCodec.decode(null)).thenReturn(null);
        when(cursorCodec.encode(new ChatRoomListCursor(LocalDateTime.of(2026, 6, 13, 14, 30), privateRoom.getId())))
                .thenReturn("next-cursor");
        when(chatRoomRepository.findActiveListPageByUserId(eq(currentUserId), eq(null), eq(null), any(Pageable.class)))
                .thenReturn(List.of(
                        new ChatRoomListQueryResult(groupCurrentMember, LocalDateTime.of(2026, 6, 13, 15, 0)),
                        new ChatRoomListQueryResult(privateCurrentMember, LocalDateTime.of(2026, 6, 13, 14, 30)),
                        new ChatRoomListQueryResult(createMember(createPrivateRoom(9L, privatePost, LocalDateTime.now()), currentUser, null), LocalDateTime.now())
                ));
        when(chatRoomMemberRepository.findByRoomIdsWithUserProfileImage(List.of(groupRoom.getId(), privateRoom.getId())))
                .thenReturn(List.of(groupCurrentMember, groupInviteeMember, privateCurrentMember, privateOpponentMember));
        when(chatRoomRepository.findLastVisibleMessagesByRoomIds(currentUserId, List.of(groupRoom.getId(), privateRoom.getId())))
                .thenReturn(List.of(groupLastMessage, privateLastMessage));
        when(chatRoomRepository.countUnreadMessagesByRoomIds(currentUserId, List.of(groupRoom.getId(), privateRoom.getId())))
                .thenReturn(Map.of(groupRoom.getId(), 5L, privateRoom.getId(), 2L));
        when(chatRoomRepository.countMembersByRoomIds(List.of(groupRoom.getId(), privateRoom.getId())))
                .thenReturn(Map.of(groupRoom.getId(), 2L, privateRoom.getId(), 2L));
        when(profileImageResolver.resolve(opponent.getProfile())).thenReturn("https://example.com/opponent.png");

        ChatRoomListResponse response = service.retrieveChatRooms(
                currentUserId,
                new ChatRoomListRequest(ChatRoomListType.ALL, 2, null)
        );

        assertThat(response.selectedRoomType()).isEqualTo(ChatRoomListType.ALL);
        assertThat(response.chatRooms()).hasSize(2);
        assertThat(response.page().hasNext()).isTrue();
        assertThat(response.page().nextCursor()).isEqualTo("next-cursor");

        assertThat(response.chatRooms().get(0).chatRoomId()).isEqualTo(groupRoom.getId());
        assertThat(response.chatRooms().get(0).roomType()).isEqualTo(ChatRoomType.GROUP);
        assertThat(response.chatRooms().get(0).displayName()).isEqualTo("제주 그룹 여정");
        assertThat(response.chatRooms().get(0).postTitle()).isNull();
        assertThat(response.chatRooms().get(0).thumbnailUrl()).isEqualTo("https://post/group.png");
        assertThat(response.chatRooms().get(0).lastMessage().content()).isEqualTo("그룹 안녕하세요");
        assertThat(response.chatRooms().get(0).lastMessage().senderId()).isEqualTo(invitee.getId());
        assertThat(response.chatRooms().get(0).unreadCount()).isEqualTo(5L);
        assertThat(response.chatRooms().get(0).activityAt()).isEqualTo(LocalDateTime.of(2026, 6, 13, 15, 0));

        assertThat(response.chatRooms().get(1).chatRoomId()).isEqualTo(privateRoom.getId());
        assertThat(response.chatRooms().get(1).displayName()).isEqualTo("guestNick");
        assertThat(response.chatRooms().get(1).postTitle()).isEqualTo("제주 애월 2박 3일");
        assertThat(response.chatRooms().get(1).thumbnailUrl()).isEqualTo("https://example.com/opponent.png");
        assertThat(response.chatRooms().get(1).lastMessage().content()).isEqualTo("안녕하세요");
        assertThat(response.chatRooms().get(1).lastMessage().senderId()).isEqualTo(opponent.getId());
        assertThat(response.chatRooms().get(1).lastReadMessageId()).isEqualTo(privateReadCursor.getId());
        assertThat(response.chatRooms().get(1).activityAt()).isEqualTo(LocalDateTime.of(2026, 6, 13, 14, 30));

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        org.mockito.Mockito.verify(chatRoomRepository)
                .findActiveListPageByUserId(eq(currentUserId), eq(null), eq(null), pageableCaptor.capture());
        assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(3);
    }

    @Test
    @DisplayName("조회된 채팅방이 없으면 부가 데이터를 조회하지 않고 빈 목록을 응답한다")
    void retrieveChatRoomsEmptyPage() {
        Long currentUserId = 10L;

        when(cursorCodec.decode(null)).thenReturn(null);
        when(chatRoomRepository.findActiveListPageByUserId(eq(currentUserId), eq(ChatRoomType.PRIVATE), eq(null), any(Pageable.class)))
                .thenReturn(List.of());

        ChatRoomListResponse response = service.retrieveChatRooms(
                currentUserId,
                new ChatRoomListRequest(ChatRoomListType.PRIVATE, 20, null)
        );

        assertThat(response.selectedRoomType()).isEqualTo(ChatRoomListType.PRIVATE);
        assertThat(response.chatRooms()).isEmpty();
        assertThat(response.page().hasNext()).isFalse();
        assertThat(response.page().nextCursor()).isNull();

        verify(chatRoomMemberRepository, never()).findByRoomIdsWithUserProfileImage(any());
        verify(chatRoomRepository, never()).findLastVisibleMessagesByRoomIds(any(), any());
        verify(chatRoomRepository, never()).countUnreadMessagesByRoomIds(any(), any());
        verify(chatRoomRepository, never()).countMembersByRoomIds(any());
    }

    @Test
    @DisplayName("단건 채팅방 목록 item도 목록 조회와 같은 정책으로 조립한다")
    void retrieveChatRoomItem() {
        Long currentUserId = 10L;
        User currentUser = createUser(currentUserId, "host", "hostNick");
        User opponent = createUser(20L, "guest", "guestNick");
        Destination destination = createDestination();
        Post privatePost = createPost(100L, currentUser, destination, "제주 애월 2박 3일", "https://post/private.png");
        ChatRoom privateRoom = createPrivateRoom(1L, privatePost, LocalDateTime.of(2026, 6, 1, 10, 0));
        ChatRoomMember currentMember = createMember(privateRoom, currentUser, null);
        ChatRoomMember opponentMember = createMember(privateRoom, opponent, null);
        ChatMessage lastMessage = createMessage(
                100L,
                privateRoom,
                opponent,
                ChatMessageType.TEXT,
                "안녕하세요",
                LocalDateTime.of(2026, 6, 13, 14, 30)
        );

        when(chatRoomRepository.findActiveListItemByUserIdAndRoomId(currentUserId, privateRoom.getId()))
                .thenReturn(Optional.of(new ChatRoomListQueryResult(
                        currentMember,
                        LocalDateTime.of(2026, 6, 13, 14, 30)
                )));
        when(chatRoomMemberRepository.findByRoomIdsWithUserProfileImage(List.of(privateRoom.getId())))
                .thenReturn(List.of(currentMember, opponentMember));
        when(chatRoomRepository.findLastVisibleMessagesByRoomIds(currentUserId, List.of(privateRoom.getId())))
                .thenReturn(List.of(lastMessage));
        when(chatRoomRepository.countUnreadMessagesByRoomIds(currentUserId, List.of(privateRoom.getId())))
                .thenReturn(Map.of(privateRoom.getId(), 2L));
        when(chatRoomRepository.countMembersByRoomIds(List.of(privateRoom.getId())))
                .thenReturn(Map.of(privateRoom.getId(), 2L));
        when(profileImageResolver.resolve(opponent.getProfile())).thenReturn("https://example.com/opponent.png");

        var response = service.retrieveChatRoomItem(currentUserId, privateRoom.getId());

        assertThat(response).isPresent();
        assertThat(response.get().chatRoomId()).isEqualTo(privateRoom.getId());
        assertThat(response.get().displayName()).isEqualTo("guestNick");
        assertThat(response.get().postTitle()).isEqualTo("제주 애월 2박 3일");
        assertThat(response.get().thumbnailUrl()).isEqualTo("https://example.com/opponent.png");
        assertThat(response.get().lastMessage().content()).isEqualTo("안녕하세요");
        assertThat(response.get().unreadCount()).isEqualTo(2L);
        assertThat(response.get().activityAt()).isEqualTo(LocalDateTime.of(2026, 6, 13, 14, 30));
    }

    private User createUser(Long id, String name, String nickname) {
        User user = User.builder()
                .email(name + id + "@example.com")
                .name(name)
                .oauthId("oauth-" + id)
                .oauthType(OauthType.KAKAO)
                .build();
        ReflectionTestUtils.setField(user, "id", id);

        Profile profile = new Profile(user);
        ReflectionTestUtils.setField(profile, "nickname", nickname);
        ReflectionTestUtils.setField(user, "profile", profile);
        return user;
    }

    private Destination createDestination() {
        Destination destination = Destination.builder()
                .countryCode("KR")
                .countryName("대한민국")
                .city("제주")
                .build();
        ReflectionTestUtils.setField(destination, "id", 1L);
        return destination;
    }

    private Post createPost(Long id, User host, Destination destination, String title, String photoUrl) {
        Post post = Post.createPost(
                host,
                destination,
                title,
                "채팅방 목록 테스트용 게시글 본문입니다.",
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(2),
                4,
                LocalDate.now().plusDays(1),
                Gender.U,
                true,
                null,
                null,
                photoUrl,
                "[]",
                null
        );
        ReflectionTestUtils.setField(post, "id", id);
        return post;
    }

    private Journey createJourney(Long id, Post post, String title, String photoUrl) {
        Journey journey = Journey.create(post);
        ReflectionTestUtils.setField(journey, "id", id);
        ReflectionTestUtils.setField(journey, "title", title);
        ReflectionTestUtils.setField(journey, "photoUrl", photoUrl);
        return journey;
    }

    private ChatRoom createPrivateRoom(Long id, Post post, LocalDateTime createdAt) {
        ChatRoom room = ChatRoom.forPrivateChat(post);
        ReflectionTestUtils.setField(room, "id", id);
        ReflectionTestUtils.setField(room, "createdAt", createdAt);
        return room;
    }

    private ChatRoom createGroupRoom(Long id, Journey journey, LocalDateTime createdAt) {
        ChatRoom room = ChatRoom.createGroupChat(journey);
        ReflectionTestUtils.setField(room, "id", id);
        ReflectionTestUtils.setField(room, "createdAt", createdAt);
        return room;
    }

    private ChatRoomMember createMember(ChatRoom room, User user, ChatMessage lastReadMessage) {
        ChatRoomMember member = ChatRoomMember.builder()
                .room(room)
                .user(user)
                .role(ChatMemberRole.GUEST)
                .lastReadMessage(lastReadMessage)
                .build();
        ReflectionTestUtils.setField(member, "createdAt", LocalDateTime.of(2026, 6, 1, 10, 0));
        return member;
    }

    private ChatMessage createMessage(
            Long id,
            ChatRoom room,
            User sender,
            ChatMessageType messageType,
            String content,
            LocalDateTime createdAt
    ) {
        ChatMessage message = ChatMessage.create(room, sender, messageType, content);
        ReflectionTestUtils.setField(message, "id", id);
        ReflectionTestUtils.setField(message, "createdAt", createdAt);
        return message;
    }
}
