package com.dduru.gildongmu.chat.service;

import com.dduru.gildongmu.chat.domain.ChatRoom;
import com.dduru.gildongmu.chat.domain.enums.ChatRoomStatus;
import com.dduru.gildongmu.chat.domain.enums.ChatRoomType;
import com.dduru.gildongmu.chat.dto.request.GroupChatInviteRequest;
import com.dduru.gildongmu.chat.dto.response.GroupChatInviteMemberResponse;
import com.dduru.gildongmu.chat.exception.ChatRoomCapacityExceededException;
import com.dduru.gildongmu.chat.exception.ChatRoomClosedException;
import com.dduru.gildongmu.chat.exception.GroupChatRoomInviteAccessDeniedException;
import com.dduru.gildongmu.chat.exception.NotSelfChatException;
import com.dduru.gildongmu.chat.repository.ChatRoomMemberRepository;
import com.dduru.gildongmu.chat.repository.ChatRoomRepository;
import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.domain.enums.OauthType;
import com.dduru.gildongmu.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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

    @Nested
    @DisplayName("roomId 진입점")
    class InviteWithRoomId {

        @Test
        @DisplayName("기존 멤버면 새 초대 없이 반환한다")
        void returnsExistingMember() {
            Long roomId = 100L;
            Long ownerId = 10L;
            Long inviteeId = 20L;
            ChatRoom room = createGroupRoom(roomId, 1L, ownerId, ChatRoomStatus.PENDING, 3);

            when(chatRoomRepository.getByIdAndRoomTypeWithLockOrThrow(roomId, ChatRoomType.GROUP)).thenReturn(room);
            when(userRepository.getByIdOrThrow(inviteeId)).thenReturn(createUser(inviteeId, "invitee"));
            when(chatRoomMemberRepository.existsByChatRoom_IdAndUser_Id(roomId, inviteeId)).thenReturn(true);

            GroupChatInviteMemberResponse response = groupChatRoomService.inviteMemberOrGetRoom(ownerId, roomId, new GroupChatInviteRequest(inviteeId));

            assertThat(response.roomId()).isEqualTo(roomId);
            assertThat(response.isNewInvitee()).isFalse();
            verify(chatRoomRepository).getByIdAndRoomTypeWithLockOrThrow(roomId, ChatRoomType.GROUP);
            verify(chatRoomMemberRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("postId 진입점")
    class InviteWithPostId {

        @Test
        @DisplayName("unique 충돌을 기존 멤버 응답으로 번역한다")
        void translatesDuplicateSave() {
            Long postId = 1L;
            Long roomId = 100L;
            Long ownerId = 10L;
            Long inviteeId = 20L;
            ChatRoom room = createGroupRoom(roomId, postId, ownerId, ChatRoomStatus.PENDING, 3);

            when(chatRoomRepository.getByPostIdAndRoomTypeWithLockOrThrow(postId, ChatRoomType.GROUP)).thenReturn(room);
            when(userRepository.getByIdOrThrow(inviteeId)).thenReturn(createUser(inviteeId, "invitee"));
            when(chatRoomMemberRepository.existsByChatRoom_IdAndUser_Id(roomId, inviteeId)).thenReturn(false, true);
            when(chatRoomMemberRepository.countByRoom(room)).thenReturn(1);
            when(chatRoomMemberRepository.save(any())).thenThrow(new DataIntegrityViolationException("duplicate"));

            GroupChatInviteMemberResponse response = groupChatRoomService.inviteMemberOrGetRoom(ownerId, postId, inviteeId);

            assertThat(response.roomId()).isEqualTo(roomId);
            assertThat(response.isNewInvitee()).isFalse();
            verify(chatRoomRepository).getByPostIdAndRoomTypeWithLockOrThrow(postId, ChatRoomType.GROUP);
        }

        @Test
        @DisplayName("정원을 초과하면 예외가 발생한다")
        void capacityExceededThrowsException() {
            Long postId = 1L;
            Long roomId = 100L;
            Long ownerId = 10L;
            Long inviteeId = 20L;
            ChatRoom room = createGroupRoom(roomId, postId, ownerId, ChatRoomStatus.ACTIVE, 2);

            when(chatRoomRepository.getByPostIdAndRoomTypeWithLockOrThrow(postId, ChatRoomType.GROUP)).thenReturn(room);
            when(userRepository.getByIdOrThrow(inviteeId)).thenReturn(createUser(inviteeId, "invitee"));
            when(chatRoomMemberRepository.existsByChatRoom_IdAndUser_Id(roomId, inviteeId)).thenReturn(false);
            when(chatRoomMemberRepository.countByRoom(room)).thenReturn(2);

            assertThatThrownBy(() -> groupChatRoomService.inviteMemberOrGetRoom(ownerId, postId, inviteeId))
                    .isInstanceOf(ChatRoomCapacityExceededException.class);
        }

        @Test
        @DisplayName("닫힌 방은 초대할 수 없다")
        void closedRoomThrowsException() {
            Long postId = 1L;
            Long ownerId = 10L;
            Long inviteeId = 20L;
            ChatRoom room = createGroupRoom(100L, postId, ownerId, ChatRoomStatus.CLOSED, 3);

            when(chatRoomRepository.getByPostIdAndRoomTypeWithLockOrThrow(postId, ChatRoomType.GROUP)).thenReturn(room);
            when(userRepository.getByIdOrThrow(inviteeId)).thenReturn(createUser(inviteeId, "invitee"));

            assertThatThrownBy(() -> groupChatRoomService.inviteMemberOrGetRoom(ownerId, postId, inviteeId))
                    .isInstanceOf(ChatRoomClosedException.class);
        }

        @Test
        @DisplayName("자기 자신은 그룹 초대할 수 없다")
        void selfInviteThrowsException() {
            Long postId = 1L;
            Long ownerId = 10L;
            ChatRoom room = createGroupRoom(100L, postId, ownerId, ChatRoomStatus.ACTIVE, 3);

            when(chatRoomRepository.getByPostIdAndRoomTypeWithLockOrThrow(postId, ChatRoomType.GROUP)).thenReturn(room);
            when(userRepository.getByIdOrThrow(ownerId)).thenReturn(createUser(ownerId, "owner"));

            assertThatThrownBy(() -> groupChatRoomService.inviteMemberOrGetRoom(ownerId, postId, ownerId))
                    .isInstanceOf(NotSelfChatException.class);
        }

        @Test
        @DisplayName("게시글 작성자가 아니면 그룹 초대할 수 없다")
        void notHostThrowsException() {
            Long postId = 1L;
            Long ownerId = 10L;
            Long requesterId = 99L;
            Long inviteeId = 20L;
            ChatRoom room = createGroupRoom(100L, postId, ownerId, ChatRoomStatus.ACTIVE, 3);

            when(chatRoomRepository.getByPostIdAndRoomTypeWithLockOrThrow(postId, ChatRoomType.GROUP)).thenReturn(room);
            when(userRepository.getByIdOrThrow(inviteeId)).thenReturn(createUser(inviteeId, "invitee"));

            assertThatThrownBy(() -> groupChatRoomService.inviteMemberOrGetRoom(requesterId, postId, inviteeId))
                    .isInstanceOf(GroupChatRoomInviteAccessDeniedException.class);
        }
    }

    private ChatRoom createGroupRoom(Long roomId, Long postId, Long ownerId, ChatRoomStatus status, int capacity) {
        User owner = createUser(ownerId, "owner");
        Post post = Post.createPost(
                owner,
                null,
                "그룹 채팅 테스트 게시글",
                "그룹 채팅 테스트 본문은 충분히 긴 내용입니다.",
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(2),
                capacity,
                LocalDate.now().plusDays(1),
                null,
                true,
                null,
                null,
                null,
                "[]",
                null
        );
        ReflectionTestUtils.setField(post, "id", postId);

        ChatRoom room = ChatRoom.createPendingGroupChat(post);
        ReflectionTestUtils.setField(room, "id", roomId);
        ReflectionTestUtils.setField(room, "status", status);
        return room;
    }

    private User createUser(Long userId, String name) {
        User user = User.builder()
                .email(name + "@example.com")
                .name(name)
                .oauthId("oauth-" + userId)
                .oauthType(OauthType.KAKAO)
                .build();
        ReflectionTestUtils.setField(user, "id", userId);
        return user;
    }
}
