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
    void inviteMembersToGroupRoom_allInviteesMissing_returnsEmptyWithoutMemberLookup() {
        Long roomId = 10L;
        Long hostId = 1L;
        GroupChatInviteRequest request = new GroupChatInviteRequest(List.of(101L, 102L));

        when(chatRoomRepository.findByIdAndRoomTypeWithPostUser(roomId, ChatRoomType.GROUP))
                .thenReturn(Optional.of(activeGroupRoom(roomId, hostId, 4)));
        when(userRepository.findAllById(request.inviteeUserIds()))
                .thenReturn(List.of());

        GroupChatInviteResponse response = groupChatRoomService.inviteMembersToGroupRoom(hostId, roomId, request);

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
    void inviteMembersToGroupRoom_mixedInvitees_returnsPartialSuccess() {
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

        GroupChatInviteResponse response = groupChatRoomService.inviteMembersToGroupRoom(hostId, roomId, request);

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

        GroupChatInviteResponse response = groupChatRoomService.inviteMembersToGroupRoom(hostId, roomId, request);

        assertThat(response.addedMemberCount()).isZero();
        assertThat(response.missingUserIds()).isEmpty();
        assertThat(response.alreadyMemberUserIds()).containsExactly(existingMemberId);
        verify(chatRoomMemberRepository, never()).countByRoom(chatRoom);
        verify(chatRoomMemberRepository, never()).saveAll(anyList());
    }

    @Test
    @DisplayName("요청자가 게시글 작성자가 아니면 초대를 거부한다")
    void inviteMembersToGroupRoom_nonHost_throwsAccessDenied() {
        Long roomId = 10L;
        GroupChatInviteRequest request = new GroupChatInviteRequest(List.of(2L));

        when(chatRoomRepository.findByIdAndRoomTypeWithPostUser(roomId, ChatRoomType.GROUP))
                .thenReturn(Optional.of(activeGroupRoom(roomId, 1L, 4)));

        assertThatThrownBy(() -> groupChatRoomService.inviteMembersToGroupRoom(99L, roomId, request))
                .isInstanceOf(GroupChatRoomInviteAccessDeniedException.class);
    }

    @Test
    @DisplayName("종료된 그룹 채팅방에는 초대할 수 없다")
    void inviteMembersToGroupRoom_closedRoom_throwsBusinessException() {
        Long roomId = 10L;
        GroupChatInviteRequest request = new GroupChatInviteRequest(List.of(2L));

        when(chatRoomRepository.findByIdAndRoomTypeWithPostUser(roomId, ChatRoomType.GROUP))
                .thenReturn(Optional.of(groupRoom(roomId, 1L, 4, ChatRoomStatus.CLOSED)));

        assertThatThrownBy(() -> groupChatRoomService.inviteMembersToGroupRoom(1L, roomId, request))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.CHAT_ROOM_CLOSED);
    }

    @Test
    @DisplayName("정원을 초과하면 예외가 발생한다")
    void inviteMembersToGroupRoom_capacityExceeded_throwsException() {
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

        assertThatThrownBy(() -> groupChatRoomService.inviteMembersToGroupRoom(hostId, roomId, request))
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
