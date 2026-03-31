package com.dduru.gildongmu.chat.service;

import com.dduru.gildongmu.chat.domain.ChatRoom;
import com.dduru.gildongmu.chat.domain.ChatRoomMember;
import com.dduru.gildongmu.chat.domain.enums.ChatMemberRole;
import com.dduru.gildongmu.chat.domain.enums.ChatRoomStatus;
import com.dduru.gildongmu.chat.domain.enums.ChatRoomType;
import com.dduru.gildongmu.chat.dto.request.GroupChatInviteRequest;
import com.dduru.gildongmu.chat.dto.response.GroupChatInviteResponse;
import com.dduru.gildongmu.chat.dto.response.PrivateChatRoomCreateResponse;
import com.dduru.gildongmu.chat.repository.ChatRoomMemberRepository;
import com.dduru.gildongmu.chat.repository.ChatRoomRepository;
import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.domain.enums.OauthType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChatRoomService 테스트")
class ChatRoomServiceTest {

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private ChatRoomMemberRepository chatRoomMemberRepository;

    @Mock
    private PrivateChatRoomService privateChatRoomService;

    @Mock
    private GroupChatRoomService groupChatRoomService;

    @InjectMocks
    private ChatRoomService chatRoomService;

    @Test
    @DisplayName("1:1 채팅방 생성 요청은 PrivateChatRoomService에 위임한다")
    void createOrGetPrivateRoom_delegatesToPrivateChatRoomService() {
        PrivateChatRoomCreateResponse expected = new PrivateChatRoomCreateResponse(7L, true);

        when(privateChatRoomService.createOrGetPrivateRoom(1L, 2L)).thenReturn(expected);

        PrivateChatRoomCreateResponse actual = chatRoomService.createOrGetPrivateRoom(1L, 2L);

        assertThat(actual).isEqualTo(expected);
        verify(privateChatRoomService).createOrGetPrivateRoom(1L, 2L);
    }

    @Test
    @DisplayName("그룹 채팅 초대 요청은 GroupChatRoomService에 위임한다")
    void inviteMembersToGroupRoom_delegatesToGroupChatRoomService() {
        GroupChatInviteRequest request = new GroupChatInviteRequest(java.util.List.of(2L, 3L));
        GroupChatInviteResponse expected = new GroupChatInviteResponse(5L, 1, java.util.List.of(3L), java.util.List.of());

        when(groupChatRoomService.inviteMembersToGroupRoom(1L, 5L, request)).thenReturn(expected);

        GroupChatInviteResponse actual = chatRoomService.inviteMembersToGroupRoom(1L, 5L, request);

        assertThat(actual).isEqualTo(expected);
        verify(groupChatRoomService).inviteMembersToGroupRoom(1L, 5L, request);
    }

    @Test
    @DisplayName("게시글용 그룹 채팅방 생성 시 방을 저장하고 작성자를 HOST로 추가한다")
    void createPendingGroupRoomForPost_savesRoomAndHostMember() {
        Post post = Post.builder()
                .title("제목")
                .recruitCapacity(3)
                .build();
        ReflectionTestUtils.setField(post, "id", 10L);
        User author = user(1L);

        when(chatRoomRepository.save(org.mockito.ArgumentMatchers.any(ChatRoom.class))).thenAnswer(invocation -> {
            ChatRoom savedRoom = invocation.getArgument(0);
            ReflectionTestUtils.setField(savedRoom, "id", 20L);
            return savedRoom;
        });

        chatRoomService.createPendingGroupRoomForPost(post, author);

        ArgumentCaptor<ChatRoom> roomCaptor = ArgumentCaptor.forClass(ChatRoom.class);
        verify(chatRoomRepository).save(roomCaptor.capture());
        ChatRoom savedRoom = roomCaptor.getValue();
        assertThat(savedRoom.getRoomType()).isEqualTo(ChatRoomType.GROUP);
        assertThat(savedRoom.getStatus()).isEqualTo(ChatRoomStatus.PENDING);
        assertThat(savedRoom.getMaxCapacity()).isEqualTo(4);

        ArgumentCaptor<ChatRoomMember> memberCaptor = ArgumentCaptor.forClass(ChatRoomMember.class);
        verify(chatRoomMemberRepository).save(memberCaptor.capture());
        ChatRoomMember savedMember = memberCaptor.getValue();
        assertThat(savedMember.getRoom()).isSameAs(savedRoom);
        assertThat(savedMember.getUser()).isSameAs(author);
        assertThat(savedMember.getRole()).isEqualTo(ChatMemberRole.HOST);
    }

    @Test
    @DisplayName("첫 메시지 이후 그룹 채팅방은 PENDING에서 ACTIVE로 전환된다")
    void activateGroupChatOnFirstMessage_activatesPendingGroupRoom() {
        ChatRoom groupRoom = ChatRoom.builder()
                .roomType(ChatRoomType.GROUP)
                .status(ChatRoomStatus.PENDING)
                .maxCapacity(4)
                .build();

        when(chatRoomRepository.getByIdOrThrow(1L)).thenReturn(groupRoom);

        chatRoomService.activateGroupChatOnFirstMessage(1L);

        assertThat(groupRoom.getStatus()).isEqualTo(ChatRoomStatus.ACTIVE);
    }

    @Test
    @DisplayName("그룹 채팅방이 아니면 첫 메시지 처리 시 상태를 변경하지 않는다")
    void activateGroupChatOnFirstMessage_nonGroupRoomKeepsStatus() {
        ChatRoom privateRoom = ChatRoom.builder()
                .roomType(ChatRoomType.PRIVATE)
                .status(ChatRoomStatus.ACTIVE)
                .maxCapacity(2)
                .build();

        when(chatRoomRepository.getByIdOrThrow(1L)).thenReturn(privateRoom);

        chatRoomService.activateGroupChatOnFirstMessage(1L);

        assertThat(privateRoom.getStatus()).isEqualTo(ChatRoomStatus.ACTIVE);
        verify(chatRoomMemberRepository, never()).save(org.mockito.ArgumentMatchers.any(ChatRoomMember.class));
    }

    private static User user(Long userId) {
        User user = User.builder()
                .email("user" + userId + "@dduru.com")
                .name("user" + userId)
                .oauthId("oauth-" + userId)
                .oauthType(OauthType.KAKAO)
                .build();
        ReflectionTestUtils.setField(user, "id", userId);
        return user;
    }
}
