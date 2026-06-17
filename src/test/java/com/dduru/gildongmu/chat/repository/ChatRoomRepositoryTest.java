package com.dduru.gildongmu.chat.repository;

import com.dduru.gildongmu.chat.domain.ChatMessage;
import com.dduru.gildongmu.chat.domain.ChatRoom;
import com.dduru.gildongmu.chat.domain.ChatRoomMember;
import com.dduru.gildongmu.chat.domain.enums.ChatMemberRole;
import com.dduru.gildongmu.chat.domain.enums.ChatMessageType;
import com.dduru.gildongmu.chat.domain.enums.ChatRoomStatus;
import com.dduru.gildongmu.chat.domain.enums.ChatRoomType;
import com.dduru.gildongmu.chat.dto.query.ChatRoomListCursor;
import com.dduru.gildongmu.chat.dto.query.ChatRoomListQueryResult;
import com.dduru.gildongmu.common.config.QueryDslConfig;
import com.dduru.gildongmu.destination.domain.Destination;
import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.profile.domain.enums.Gender;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.domain.enums.OauthType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(QueryDslConfig.class)
@DisplayName("ChatRoomRepository 테스트")
class ChatRoomRepositoryTest {

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("현재 사용자의 ACTIVE 방을 activityAt DESC, roomId DESC로 cursor 조회한다")
    void findActiveListPageByUserIdOrdersByActivityAt() {
        User currentUser = persistUser("current", 1);
        User opponent = persistUser("opponent", 2);
        Destination destination = persistDestination();
        Post post = persistPost(currentUser, destination);

        ChatRoom room1 = persistPrivateRoom(post, ChatRoomStatus.ACTIVE);
        ChatRoom room2 = persistPrivateRoom(post, ChatRoomStatus.ACTIVE);
        ChatRoom room3 = persistPrivateRoom(post, ChatRoomStatus.ACTIVE);
        ChatRoom otherUserRoom = persistPrivateRoom(post, ChatRoomStatus.ACTIVE);
        ChatRoom deletedRoom = persistPrivateRoom(post, ChatRoomStatus.DELETED);

        ChatRoomMember room1CurrentMember = persistMember(room1, currentUser);
        persistMember(room1, opponent);
        ChatRoomMember room2CurrentMember = persistMember(room2, currentUser);
        persistMember(room2, opponent);
        ChatRoomMember room3CurrentMember = persistMember(room3, currentUser);
        persistMember(room3, opponent);
        persistMember(otherUserRoom, opponent);
        persistMember(deletedRoom, currentUser);

        ChatMessage room1Message = persistMessage(room1, opponent, "room1");
        ChatMessage room1SystemMessage = persistMessage(room1, null, ChatMessageType.SYSTEM, "system");
        ChatMessage room2Message = persistMessage(room2, opponent, "room2");
        ChatMessage room3OldMessage = persistMessage(room3, opponent, "old");
        persistMessage(otherUserRoom, opponent, "other");
        persistMessage(deletedRoom, opponent, "deleted");

        entityManager.flush();
        updateCreatedAt("chat_rooms", room1.getId(), LocalDateTime.of(2026, 6, 13, 10, 0));
        updateCreatedAt("chat_rooms", room2.getId(), LocalDateTime.of(2026, 6, 13, 10, 0));
        updateCreatedAt("chat_rooms", room3.getId(), LocalDateTime.of(2026, 6, 13, 13, 0));
        updateCreatedAt("chat_rooms", otherUserRoom.getId(), LocalDateTime.of(2026, 6, 13, 10, 0));
        updateCreatedAt("chat_rooms", deletedRoom.getId(), LocalDateTime.of(2026, 6, 13, 10, 0));
        updateCreatedAt("chat_room_members", room1CurrentMember.getId(), LocalDateTime.of(2026, 6, 13, 9, 0));
        updateCreatedAt("chat_room_members", room2CurrentMember.getId(), LocalDateTime.of(2026, 6, 13, 9, 0));
        updateCreatedAt("chat_room_members", room3CurrentMember.getId(), LocalDateTime.of(2026, 6, 13, 13, 0));
        updateCreatedAt("chat_messages", room1Message.getId(), LocalDateTime.of(2026, 6, 13, 15, 0));
        updateCreatedAt("chat_messages", room1SystemMessage.getId(), LocalDateTime.of(2026, 6, 13, 15, 30));
        updateCreatedAt("chat_messages", room2Message.getId(), LocalDateTime.of(2026, 6, 13, 14, 0));
        updateCreatedAt("chat_messages", room3OldMessage.getId(), LocalDateTime.of(2026, 6, 13, 8, 0));
        updateLastReadMessage(room2CurrentMember.getId(), room2Message.getId());
        entityManager.clear();

        List<ChatRoomListQueryResult> firstPage = chatRoomRepository.findActiveListPageByUserId(
                currentUser.getId(),
                null,
                null,
                PageRequest.of(0, 2)
        );

        assertThat(firstPage).hasSize(2);
        assertThat(firstPage).extracting(result -> result.currentMember().getRoom().getId())
                .containsExactly(room1.getId(), room2.getId());
        assertThat(firstPage).extracting(ChatRoomListQueryResult::activityAt)
                .containsExactly(
                        LocalDateTime.of(2026, 6, 13, 15, 0),
                        LocalDateTime.of(2026, 6, 13, 14, 0)
                );

        List<ChatRoomListQueryResult> nextPage = chatRoomRepository.findActiveListPageByUserId(
                currentUser.getId(),
                ChatRoomType.PRIVATE,
                new ChatRoomListCursor(LocalDateTime.of(2026, 6, 13, 14, 0), room2.getId()),
                PageRequest.of(0, 10)
        );

        assertThat(nextPage).hasSize(1);
        assertThat(nextPage.get(0).currentMember().getRoom().getId()).isEqualTo(room3.getId());
        assertThat(nextPage.get(0).activityAt()).isEqualTo(LocalDateTime.of(2026, 6, 13, 13, 0));

        Map<Long, ChatMessage> lastMessages = chatRoomRepository.findLastVisibleMessagesByRoomIds(
                        currentUser.getId(),
                        List.of(room1.getId(), room2.getId(), room3.getId())
                ).stream()
                .collect(Collectors.toMap(message -> message.getRoom().getId(), Function.identity()));
        assertThat(lastMessages).containsOnlyKeys(room1.getId(), room2.getId());
        assertThat(lastMessages.get(room1.getId()).getId()).isEqualTo(room1Message.getId());
        assertThat(lastMessages.get(room2.getId()).getId()).isEqualTo(room2Message.getId());

        Map<Long, Long> unreadCounts = chatRoomRepository.countUnreadMessagesByRoomIds(
                currentUser.getId(),
                List.of(room1.getId(), room2.getId(), room3.getId())
        );
        assertThat(unreadCounts).containsEntry(room1.getId(), 1L);
        assertThat(unreadCounts).doesNotContainKey(room2.getId());
        assertThat(unreadCounts).doesNotContainKey(room3.getId());

        Map<Long, Long> memberCounts = chatRoomRepository.countMembersByRoomIds(
                List.of(room1.getId(), room2.getId(), room3.getId())
        );
        assertThat(memberCounts).containsEntry(room1.getId(), 2L);
        assertThat(memberCounts).containsEntry(room2.getId(), 2L);
        assertThat(memberCounts).containsEntry(room3.getId(), 2L);
    }

    private User persistUser(String name, int suffix) {
        User user = User.builder()
                .email(name + suffix + "@example.com")
                .name(name)
                .oauthId("oauth-" + suffix)
                .oauthType(OauthType.KAKAO)
                .build();
        return entityManager.persist(user);
    }

    private Destination persistDestination() {
        return entityManager.persist(Destination.builder()
                .countryCode("KR")
                .countryName("대한민국")
                .city("제주")
                .build());
    }

    private Post persistPost(User host, Destination destination) {
        Post post = Post.createPost(
                host,
                destination,
                "제주 여행",
                "채팅방 repository 테스트용 게시글 본문입니다.",
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
        return entityManager.persist(post);
    }

    private ChatRoom persistPrivateRoom(Post post, ChatRoomStatus status) {
        ChatRoom room = ChatRoom.forPrivateChat(post);
        ReflectionTestUtils.setField(room, "status", status);
        return entityManager.persist(room);
    }

    private ChatRoomMember persistMember(ChatRoom room, User user) {
        return entityManager.persist(ChatRoomMember.create(room, user, ChatMemberRole.GUEST));
    }

    private ChatMessage persistMessage(ChatRoom room, User sender, String content) {
        return persistMessage(room, sender, ChatMessageType.TEXT, content);
    }

    private ChatMessage persistMessage(ChatRoom room, User sender, ChatMessageType messageType, String content) {
        return entityManager.persist(ChatMessage.create(room, sender, messageType, content));
    }

    private void updateCreatedAt(String tableName, Long id, LocalDateTime createdAt) {
        entityManager.getEntityManager()
                .createNativeQuery("UPDATE " + tableName + " SET created_at = ? WHERE id = ?")
                .setParameter(1, createdAt)
                .setParameter(2, id)
                .executeUpdate();
    }

    private void updateLastReadMessage(Long memberId, Long messageId) {
        entityManager.getEntityManager()
                .createNativeQuery("UPDATE chat_room_members SET last_read_message_id = ? WHERE id = ?")
                .setParameter(1, messageId)
                .setParameter(2, memberId)
                .executeUpdate();
    }
}
