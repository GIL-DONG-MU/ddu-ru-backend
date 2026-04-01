package com.dduru.gildongmu.chat.service;

import com.dduru.gildongmu.chat.domain.ChatRoom;
import com.dduru.gildongmu.chat.domain.ChatRoomMember;
import com.dduru.gildongmu.chat.domain.enums.ChatMemberRole;
import com.dduru.gildongmu.chat.domain.enums.ChatRoomStatus;
import com.dduru.gildongmu.chat.domain.enums.ChatRoomType;
import com.dduru.gildongmu.chat.dto.request.GroupChatInviteRequest;
import com.dduru.gildongmu.chat.dto.response.GroupChatInviteResponse;
import com.dduru.gildongmu.chat.exception.ChatRoomCapacityExceededException;
import com.dduru.gildongmu.chat.exception.GroupChatRoomInviteAccessDeniedException;
import com.dduru.gildongmu.chat.repository.ChatRoomMemberRepository;
import com.dduru.gildongmu.chat.repository.ChatRoomRepository;
import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;
import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.domain.enums.OauthType;
import com.dduru.gildongmu.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("GroupChatRoomService 테스트")
class GroupChatRoomServiceTest {

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private ChatRoomMemberRepository chatRoomMemberRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private GroupChatRoomService groupChatRoomService;

    @Test
    @DisplayName("초대 대상이 모두 존재하지 않으면 응답만 반환하고 멤버 조회 쿼리를 실행하지 않는다")
    void inviteMembers_allInviteesMissing_returnsEmptyWithoutMemberLookup() {
        Long roomId = 10L;
        Long hostId = 1L;
        GroupChatInviteRequest request = new GroupChatInviteRequest(List.of(101L, 102L));

        when(chatRoomRepository.findByIdAndRoomTypeWithPostUser(roomId, ChatRoomType.GROUP))
                .thenReturn(Optional.of(activeGroupRoom(roomId, hostId, 4)));
        when(userRepository.findAllById(request.inviteeUserIds()))
                .thenReturn(List.of());

        GroupChatInviteResponse response = groupChatRoomService.inviteMembers(hostId, roomId, request);

        assertThat(response.roomId()).isEqualTo(roomId);
        assertThat(response.addedMemberCount()).isZero();
        assertThat(response.missingUserIds()).containsExactly(101L, 102L);
        assertThat(response.alreadyMemberUserIds()).isEmpty();
        verify(chatRoomMemberRepository, never()).findExistingUserIdsByRoomIdAndUserIdIn(anyLong(), anyCollection());
        verify(chatRoomMemberRepository, never()).countByRoom(org.mockito.ArgumentMatchers.any(ChatRoom.class));
        verify(chatRoomMemberRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("존재하는 사용자와 없는 사용자가 섞여 있으면 유효한 사용자만 초대하고 누락 목록을 응답에 담는다")
    void inviteMembers_mixedInvitees_returnsPartialSuccess() {
        Long roomId = 10L;
        Long hostId = 1L;
        Long validUserId = 2L;
        Long missingUserId = 99L;
        GroupChatInviteRequest request = new GroupChatInviteRequest(List.of(validUserId, missingUserId));
        ChatRoom chatRoom = activeGroupRoom(roomId, hostId, 4);
        User validUser = user(validUserId);

        when(chatRoomRepository.findByIdAndRoomTypeWithPostUser(roomId, ChatRoomType.GROUP))
                .thenReturn(Optional.of(chatRoom));
        when(userRepository.findAllById(request.inviteeUserIds()))
                .thenReturn(List.of(validUser));
        when(chatRoomMemberRepository.findExistingUserIdsByRoomIdAndUserIdIn(roomId, Set.of(validUserId)))
                .thenReturn(List.of());
        when(chatRoomMemberRepository.countByRoom(chatRoom)).thenReturn(1);

        GroupChatInviteResponse response = groupChatRoomService.inviteMembers(hostId, roomId, request);

        assertThat(response.roomId()).isEqualTo(roomId);
        assertThat(response.addedMemberCount()).isEqualTo(1);
        assertThat(response.missingUserIds()).containsExactly(missingUserId);
        assertThat(response.alreadyMemberUserIds()).isEmpty();

        ArgumentCaptor<List<ChatRoomMember>> membersCaptor = ArgumentCaptor.forClass(List.class);
        verify(chatRoomMemberRepository).saveAll(membersCaptor.capture());
        List<ChatRoomMember> savedMembers = membersCaptor.getValue();
        assertThat(savedMembers).hasSize(1);
        assertThat(savedMembers.get(0).getUser()).isSameAs(validUser);
        assertThat(savedMembers.get(0).getRole()).isEqualTo(ChatMemberRole.GUEST);
    }

    @Test
    @DisplayName("이미 멤버인 사용자만 초대하면 추가 없이 이미 멤버 목록만 반환한다")
    void inviteMembersToGroupRoom_alreadyMembersOnly_returnsNoChange() {
        Long roomId = 10L;
        Long hostId = 1L;
        Long existingMemberId = 2L;
        GroupChatInviteRequest request = new GroupChatInviteRequest(List.of(existingMemberId));
        ChatRoom chatRoom = activeGroupRoom(roomId, hostId, 4);
        User existingUser = user(existingMemberId);

        when(chatRoomRepository.findByIdAndRoomTypeWithPostUser(roomId, ChatRoomType.GROUP))
                .thenReturn(Optional.of(chatRoom));
        when(userRepository.findAllById(request.inviteeUserIds()))
                .thenReturn(List.of(existingUser));
        when(chatRoomMemberRepository.findExistingUserIdsByRoomIdAndUserIdIn(roomId, Set.of(existingMemberId)))
                .thenReturn(List.of(existingMemberId));

        GroupChatInviteResponse response = groupChatRoomService.inviteMembers(hostId, roomId, request);

        assertThat(response.addedMemberCount()).isZero();
        assertThat(response.missingUserIds()).isEmpty();
        assertThat(response.alreadyMemberUserIds()).containsExactly(existingMemberId);
        verify(chatRoomMemberRepository, never()).countByRoom(chatRoom);
        verify(chatRoomMemberRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("요청자가 게시글 작성자가 아니면 초대를 거부한다")
    void inviteMembers_nonHost_throwsAccessDenied() {
        Long roomId = 10L;
        GroupChatInviteRequest request = new GroupChatInviteRequest(List.of(2L));

        when(chatRoomRepository.findByIdAndRoomTypeWithPostUser(roomId, ChatRoomType.GROUP))
                .thenReturn(Optional.of(activeGroupRoom(roomId, 1L, 4)));

        assertThatThrownBy(() -> groupChatRoomService.inviteMembers(99L, roomId, request))
                .isInstanceOf(GroupChatRoomInviteAccessDeniedException.class);
    }

    @Test
    @DisplayName("종료된 그룹 채팅방에는 초대할 수 없다")
    void inviteMembers_throwsBusinessException() {
        Long roomId = 10L;
        GroupChatInviteRequest request = new GroupChatInviteRequest(List.of(2L));

        when(chatRoomRepository.findByIdAndRoomTypeWithPostUser(roomId, ChatRoomType.GROUP))
                .thenReturn(Optional.of(groupRoom(roomId, 1L, 4, ChatRoomStatus.CLOSED)));

        assertThatThrownBy(() -> groupChatRoomService.inviteMembers(1L, roomId, request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.CHAT_ROOM_CLOSED);
    }

    @Test
    @DisplayName("정원을 초과하면 예외가 발생한다")
    void inviteMembers_capacityExceeded_throwsException() {
        Long roomId = 10L;
        Long hostId = 1L;
        Long inviteeId = 2L;
        GroupChatInviteRequest request = new GroupChatInviteRequest(List.of(inviteeId));
        ChatRoom chatRoom = activeGroupRoom(roomId, hostId, 1);
        User invitee = user(inviteeId);

        when(chatRoomRepository.findByIdAndRoomTypeWithPostUser(roomId, ChatRoomType.GROUP))
                .thenReturn(Optional.of(chatRoom));
        when(userRepository.findAllById(request.inviteeUserIds()))
                .thenReturn(List.of(invitee));
        when(chatRoomMemberRepository.findExistingUserIdsByRoomIdAndUserIdIn(roomId, Set.of(inviteeId)))
                .thenReturn(List.of());
        when(chatRoomMemberRepository.countByRoom(chatRoom)).thenReturn(1);

        assertThatThrownBy(() -> groupChatRoomService.inviteMembers(hostId, roomId, request))
                .isInstanceOf(ChatRoomCapacityExceededException.class);
    }

    private static ChatRoom activeGroupRoom(Long roomId, Long hostId, int maxCapacity) {
        return groupRoom(roomId, hostId, maxCapacity, ChatRoomStatus.ACTIVE);
    }

    private static ChatRoom groupRoom(Long roomId, Long hostId, int maxCapacity, ChatRoomStatus status) {
        Post post = Post.builder()
                .user(user(hostId))
                .build();
        ChatRoom chatRoom = ChatRoom.builder()
                .post(post)
                .roomType(ChatRoomType.GROUP)
                .status(status)
                .maxCapacity(maxCapacity)
                .build();
        ReflectionTestUtils.setField(chatRoom, "id", roomId);
        return chatRoom;
    }

/*
    @Test
    @DisplayName("그룹 채팅 초대 요청은 GroupChatRoomService에 위임한다")
    void inviteMembersToGroupRoom_delegatesToGroupChatRoomService() {
        GroupChatInviteRequest request = new GroupChatInviteRequest(java.util.List.of(2L, 3L));
        GroupChatInviteResponse expected = new GroupChatInviteResponse(5L, 1, java.util.List.of(3L), java.util.List.of());

        when(groupChatRoomService.inviteMembersToGroupRoom(1L, 5L, request)).thenReturn(expected);

        GroupChatInviteResponse actual = groupChatRoomService.inviteMembersToGroupRoom(1L, 5L, request);

        assertThat(actual).isEqualTo(expected);
        verify(groupChatRoomService).inviteMembersToGroupRoom(1L, 5L, request);
    }
*/

    @Test
    @DisplayName("게시글용 그룹 채팅방 생성 시 방을 저장하고 작성자를 HOST로 추가한다")
    void createPendingRoomForPost_savesRoomAndHostMember() {
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

        groupChatRoomService.createPendingRoomForPost(post, author);

        ArgumentCaptor<ChatRoom> roomCaptor = ArgumentCaptor.forClass(ChatRoom.class);
        verify(chatRoomRepository).save(roomCaptor.capture());
        ChatRoom savedRoom = roomCaptor.getValue();
        assertThat(savedRoom.getRoomType()).isEqualTo(ChatRoomType.GROUP);
        assertThat(savedRoom.getStatus()).isEqualTo(ChatRoomStatus.PENDING);
        assertThat(savedRoom.getMaxCapacity()).isEqualTo(3);

        ArgumentCaptor<ChatRoomMember> memberCaptor = ArgumentCaptor.forClass(ChatRoomMember.class);
        verify(chatRoomMemberRepository).save(memberCaptor.capture());
        ChatRoomMember savedMember = memberCaptor.getValue();
        assertThat(savedMember.getRoom()).isSameAs(savedRoom);
        assertThat(savedMember.getUser()).isSameAs(author);
        assertThat(savedMember.getRole()).isEqualTo(ChatMemberRole.HOST);
    }

    @Test
    @DisplayName("첫 메시지 이후 그룹 채팅방은 PENDING에서 ACTIVE로 전환된다")
    void activateGroupChatOnFirstMessage_activatesPendingRoom() {
        ChatRoom groupRoom = ChatRoom.builder()
                .roomType(ChatRoomType.GROUP)
                .status(ChatRoomStatus.PENDING)
                .maxCapacity(4)
                .build();

        when(chatRoomRepository.getByIdOrThrow(1L)).thenReturn(groupRoom);

         groupChatRoomService.activateChatOnFirstMessage(1L);

        assertThat(groupRoom.getStatus()).isEqualTo(ChatRoomStatus.ACTIVE);
    }

    @Test
    @DisplayName("그룹 채팅방이 아니면 첫 메시지 처리 시 상태를 변경하지 않는다")
    void activateGroupChatOnFirstMessage_nonRoomKeepsStatus() {
        ChatRoom privateRoom = ChatRoom.builder()
                .roomType(ChatRoomType.PRIVATE)
                .status(ChatRoomStatus.ACTIVE)
                .maxCapacity(2)
                .build();

        when(chatRoomRepository.getByIdOrThrow(1L)).thenReturn(privateRoom);

        groupChatRoomService.activateChatOnFirstMessage(1L);

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
