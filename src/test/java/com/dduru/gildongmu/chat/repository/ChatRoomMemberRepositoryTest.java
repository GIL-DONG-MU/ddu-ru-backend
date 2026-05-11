package com.dduru.gildongmu.chat.repository;

import com.dduru.gildongmu.chat.domain.ChatMessage;
import com.dduru.gildongmu.chat.domain.ChatRoom;
import com.dduru.gildongmu.chat.domain.ChatRoomMember;
import com.dduru.gildongmu.chat.domain.enums.ChatMemberRole;
import com.dduru.gildongmu.chat.domain.enums.ChatMessageType;
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
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(QueryDslConfig.class)
@DisplayName("ChatRoomMemberRepository 테스트")
class ChatRoomMemberRepositoryTest {

    @Autowired
    private ChatRoomMemberRepository chatRoomMemberRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("findByRoomIdWithLastReadMessage는 현재 방 멤버와 lastReadMessage를 함께 조회한다")
    void findByRoomIdWithLastReadMessageFetchesReadCursor() {
        User host = persistUser("host", 1);
        User guest = persistUser("guest", 2);
        Destination destination = persistDestination();
        Post post = persistPost(host, destination);
        ChatRoom room = entityManager.persist(ChatRoom.forPrivateChat(post));
        ChatRoom otherRoom = entityManager.persist(ChatRoom.forPrivateChat(post));
        ChatMessage message = entityManager.persist(ChatMessage.create(room, host, ChatMessageType.TEXT, "target"));
        ChatMessage otherMessage = entityManager.persist(ChatMessage.create(otherRoom, guest, ChatMessageType.TEXT, "other"));

        ChatRoomMember hostMember = entityManager.persist(ChatRoomMember.builder()
                .room(room)
                .user(host)
                .role(ChatMemberRole.HOST)
                .lastReadMessage(message)
                .build());
        entityManager.persist(ChatRoomMember.builder()
                .room(room)
                .user(guest)
                .role(ChatMemberRole.GUEST)
                .build());
        entityManager.persist(ChatRoomMember.builder()
                .room(otherRoom)
                .user(host)
                .role(ChatMemberRole.HOST)
                .lastReadMessage(otherMessage)
                .build());
        entityManager.flush();
        entityManager.clear();

        List<ChatRoomMember> members = chatRoomMemberRepository.findByRoomIdWithLastReadMessage(room.getId());

        assertThat(members).hasSize(2);
        assertThat(members).extracting(member -> member.getUser().getId())
                .containsExactlyInAnyOrder(host.getId(), guest.getId());
        ChatRoomMember foundHost = members.stream()
                .filter(member -> member.getUser().getId().equals(hostMember.getUser().getId()))
                .findFirst()
                .orElseThrow();
        assertThat(foundHost.getLastReadMessage().getId()).isEqualTo(message.getId());
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
                "채팅방 멤버 repository 테스트용 본문입니다.",
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
}
