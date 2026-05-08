package com.dduru.gildongmu.chat.domain;

import com.dduru.gildongmu.chat.domain.enums.ChatRoomType;
import com.dduru.gildongmu.chat.domain.enums.ChatRoomStatus;
import com.dduru.gildongmu.chat.exception.InvalidChatRoomContextException;
import com.dduru.gildongmu.common.exception.ErrorCode;
import com.dduru.gildongmu.journey.domain.Journey;
import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.domain.enums.OauthType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowableOfType;

@DisplayName("ChatRoom 도메인 테스트")
class ChatRoomTest {

    @Nested
    @DisplayName("채팅방 컨텍스트 검증")
    class ValidateRoomContext {

        @Test
        @DisplayName("채팅방 타입이 없으면 커스텀 예외가 발생한다")
        void missingRoomTypeThrowsCustomException() {
            InvalidChatRoomContextException exception = catchThrowableOfType(
                    () -> createChatRoom(null, null, null),
                    InvalidChatRoomContextException.class
            );

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.CHAT_ROOM_TYPE_REQUIRED);
            assertThat(exception).hasMessage("채팅방 타입은 필수입니다.");
        }

        @Test
        @DisplayName("1:1 채팅방이 post 이외의 컨텍스트를 가지면 커스텀 예외가 발생한다")
        void invalidPrivateRoomContextThrowsCustomException() {
            Journey journey = Journey.create(createPost());

            InvalidChatRoomContextException exception = catchThrowableOfType(
                    () -> createChatRoom(null, journey, ChatRoomType.PRIVATE),
                    InvalidChatRoomContextException.class
            );

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.PRIVATE_CHAT_ROOM_INVALID_CONTEXT);
            assertThat(exception).hasMessage("1:1 채팅방은 게시글만 참조해야 합니다.");
        }

        @Test
        @DisplayName("그룹 채팅방이 journey 이외의 컨텍스트를 가지면 커스텀 예외가 발생한다")
        void invalidGroupRoomContextThrowsCustomException() {
            Post post = createPost();

            InvalidChatRoomContextException exception = catchThrowableOfType(
                    () -> createChatRoom(post, null, ChatRoomType.GROUP),
                    InvalidChatRoomContextException.class
            );

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.GROUP_CHAT_ROOM_INVALID_CONTEXT);
            assertThat(exception).hasMessage("그룹 채팅방은 나의 여정만 참조해야 합니다.");
        }

        @Test
        @DisplayName("그룹 채팅방 생성 시 journey가 없으면 커스텀 예외가 발생한다")
        void missingJourneyInGroupFactoryThrowsCustomException() {
            InvalidChatRoomContextException exception = catchThrowableOfType(
                    () -> ChatRoom.createGroupChat(null),
                    InvalidChatRoomContextException.class
            );

            assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.GROUP_CHAT_ROOM_INVALID_CONTEXT);
            assertThat(exception).hasMessage("그룹 채팅방은 나의 여정만 참조해야 합니다.");
        }
    }

    @Nested
    @DisplayName("그룹 채팅방 정원")
    class GroupRoomCapacity {

        @Test
        @DisplayName("그룹 채팅방 정원은 연결된 게시글 모집 정원에서 계산한다")
        void groupRoomCapacityIsDerivedFromPostRecruitCapacity() {
            Post post = createPost();
            ChatRoom room = ChatRoom.createGroupChat(Journey.create(post));

            assertThat(room.canAccommodate(3)).isTrue();
            assertThat(room.canAccommodate(4)).isFalse();

            ReflectionTestUtils.setField(post, "recruitCapacity", 4);

            assertThat(room.canAccommodate(4)).isTrue();
        }
    }

    @Nested
    @DisplayName("1:1 채팅방 정원")
    class PrivateRoomCapacity {

        @Test
        @DisplayName("1:1 채팅방은 항상 2명까지만 수용한다")
        void privateRoomCapacityIsAlwaysTwo() {
            ChatRoom room = ChatRoom.forPrivateChat(createPost());

            assertThat(room.canAccommodate(2)).isTrue();
            assertThat(room.canAccommodate(3)).isFalse();
        }
    }

    private ChatRoom createChatRoom(Post post, Journey journey, ChatRoomType roomType) {
        try {
            Constructor<ChatRoom> constructor = ChatRoom.class.getDeclaredConstructor(
                    Post.class,
                    Journey.class,
                    ChatRoomType.class,
                    ChatRoomStatus.class
            );
            constructor.setAccessible(true);
            return constructor.newInstance(post, journey, roomType, null);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new RuntimeException(cause);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private Post createPost() {
        return Post.createPost(
                createUser(),
                null,
                "채팅방 컨텍스트 테스트 게시글",
                "채팅방 컨텍스트 테스트 본문은 충분히 긴 내용입니다.",
                LocalDate.now().plusDays(1),
                LocalDate.now().plusDays(2),
                3,
                LocalDate.now().plusDays(1),
                null,
                true,
                null,
                null,
                null,
                "[]",
                null
        );
    }

    private User createUser() {
        return User.builder()
                .email("chat-room-test@example.com")
                .name("chat-room-test")
                .oauthId("oauth-chat-room-test")
                .oauthType(OauthType.KAKAO)
                .build();
    }
}
