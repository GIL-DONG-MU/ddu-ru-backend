package com.dduru.gildongmu.chat.service;

import com.dduru.gildongmu.chat.domain.ChatRoom;
import com.dduru.gildongmu.chat.domain.ChatRoomMember;
import com.dduru.gildongmu.chat.domain.enums.ChatMemberRole;
import com.dduru.gildongmu.chat.domain.enums.ChatRoomStatus;
import com.dduru.gildongmu.chat.domain.enums.ChatRoomType;
import com.dduru.gildongmu.chat.dto.response.PrivateChatRoomCreateResponse;
import com.dduru.gildongmu.chat.exception.NotSelfChatException;
import com.dduru.gildongmu.chat.repository.ChatRoomMemberRepository;
import com.dduru.gildongmu.chat.repository.ChatRoomRepository;
import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.post.repository.PostRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PrivateChatRoomService 테스트")
class PrivateChatRoomServiceTest {

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private ChatRoomMemberRepository chatRoomMemberRepository;

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PrivateChatRoomService privateChatRoomService;

    @Test
    @DisplayName("기존 1:1 채팅방이 있으면 새로 만들지 않고 기존 방을 반환한다")
    void createOrGetPrivateRoom_existingRoom_returnsExistingRoom() {
        Long requesterId = 1L;
        Long postId = 10L;
        User requester = user(requesterId);
        User target = user(2L);
        Post post = post(postId, target, 3);
        ChatRoom existingRoom = privateRoom(post, 20L);

        when(postRepository.getActiveByIdOrThrow(postId)).thenReturn(post);
        when(userRepository.getByIdOrThrow(requesterId)).thenReturn(requester);
        when(chatRoomRepository.findPrivateRoomByPostAndUsers(postId, ChatRoomType.PRIVATE, ChatRoomStatus.ACTIVE, requester, target))
                .thenReturn(Optional.of(existingRoom));

        PrivateChatRoomCreateResponse response = privateChatRoomService.createOrGetPrivateRoom(requesterId, postId);

        assertThat(response.roomId()).isEqualTo(20L);
        assertThat(response.isCreated()).isFalse();
        verify(chatRoomRepository, never()).save(any(ChatRoom.class));
        verify(chatRoomMemberRepository, never()).saveAll(any());
    }

    @Test
    @DisplayName("기존 1:1 채팅방이 없으면 새 방과 멤버를 생성한다")
    void createOrGetPrivateRoom_newRoom_createsRoomAndMembers() {
        Long requesterId = 1L;
        Long postId = 10L;
        User requester = user(requesterId);
        User target = user(2L);
        Post post = post(postId, target, 3);

        when(postRepository.getActiveByIdOrThrow(postId)).thenReturn(post);
        when(userRepository.getByIdOrThrow(requesterId)).thenReturn(requester);
        when(chatRoomRepository.findPrivateRoomByPostAndUsers(postId, ChatRoomType.PRIVATE, ChatRoomStatus.ACTIVE, requester, target))
                .thenReturn(Optional.empty());
        when(chatRoomRepository.save(any(ChatRoom.class))).thenAnswer(invocation -> {
            ChatRoom room = invocation.getArgument(0);
            ReflectionTestUtils.setField(room, "id", 30L);
            return room;
        });

        PrivateChatRoomCreateResponse response = privateChatRoomService.createOrGetPrivateRoom(requesterId, postId);

        assertThat(response.roomId()).isEqualTo(30L);
        assertThat(response.isCreated()).isTrue();

        ArgumentCaptor<List<ChatRoomMember>> membersCaptor = ArgumentCaptor.forClass(List.class);
        verify(chatRoomMemberRepository).saveAll(membersCaptor.capture());
        List<ChatRoomMember> savedMembers = membersCaptor.getValue();
        assertThat(savedMembers).hasSize(2);
        assertThat(savedMembers).extracting(ChatRoomMember::getUser).containsExactly(requester, target);
        assertThat(savedMembers).extracting(ChatRoomMember::getRole)
                .containsExactly(ChatMemberRole.HOST, ChatMemberRole.GUEST);
    }

    @Test
    @DisplayName("자신과의 1:1 채팅 생성은 허용하지 않는다")
    void createOrGetPrivateRoom_selfChat_throwsException() {
        Long requesterId = 1L;
        Long postId = 10L;
        User self = user(requesterId);
        Post post = post(postId, self, 3);

        when(postRepository.getActiveByIdOrThrow(postId)).thenReturn(post);
        when(userRepository.getByIdOrThrow(requesterId)).thenReturn(self);

        assertThatThrownBy(() -> privateChatRoomService.createOrGetPrivateRoom(requesterId, postId))
                .isInstanceOf(NotSelfChatException.class);

        verify(chatRoomRepository, never()).findPrivateRoomByPostAndUsers(any(), any(), any(), any(), any());
        verify(chatRoomRepository, never()).save(any(ChatRoom.class));
        verify(chatRoomMemberRepository, never()).saveAll(any());
    }

    private static Post post(Long postId, User author, int recruitCapacity) {
        Post post = Post.builder()
                .user(author)
                .title("제목")
                .recruitCapacity(recruitCapacity)
                .build();
        ReflectionTestUtils.setField(post, "id", postId);
        return post;
    }

    private static ChatRoom privateRoom(Post post, Long roomId) {
        ChatRoom room = ChatRoom.builder()
                .post(post)
                .roomType(ChatRoomType.PRIVATE)
                .status(ChatRoomStatus.ACTIVE)
                .maxCapacity(2)
                .build();
        ReflectionTestUtils.setField(room, "id", roomId);
        return room;
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
