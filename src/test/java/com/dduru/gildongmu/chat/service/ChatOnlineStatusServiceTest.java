package com.dduru.gildongmu.chat.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ChatOnlineStatusService 테스트")
class ChatOnlineStatusServiceTest {

    @Mock private RedisTemplate<String, String> redisTemplate;
    @Mock private SetOperations<String, String> setOps;
    @Mock private ValueOperations<String, String> valueOps;

    private ChatOnlineStatusService service;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForSet()).thenReturn(setOps);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        service = new ChatOnlineStatusService(redisTemplate);
    }

    @Nested
    @DisplayName("enter — 채팅방 입장")
    class Enter {

        @Test
        @DisplayName("유저를 online 셋에 추가하고 세션 매핑을 저장한다")
        void addsUserToOnlineSetAndStoresSession() {
            service.enter(5L, 10L, "session-1");

            verify(setOps).add("chat:online:5", "10");
            verify(redisTemplate).expire(eq("chat:online:5"), eq(Duration.ofHours(2)));
            verify(valueOps).set("chat:session:session-1", "10:5", Duration.ofHours(2));
        }
    }

    @Nested
    @DisplayName("leave — 채팅방 퇴장")
    class Leave {

        @Test
        @DisplayName("유저를 online 셋에서 제거하고 세션 매핑을 삭제한다")
        void removesUserFromOnlineSetAndDeletesSession() {
            service.leave(5L, 10L, "session-1");

            verify(setOps).remove("chat:online:5", "10");
            verify(redisTemplate).delete("chat:session:session-1");
        }
    }

    @Nested
    @DisplayName("disconnect — 연결 종료")
    class Disconnect {

        @Test
        @DisplayName("세션 매핑을 조회해 online 셋에서 제거하고 세션 키를 삭제한다")
        void cleansUpPresenceOnDisconnect() {
            when(valueOps.get("chat:session:session-1")).thenReturn("10:5");

            service.disconnect("session-1");

            verify(setOps).remove("chat:online:5", "10");
            verify(redisTemplate).delete("chat:session:session-1");
        }

        @Test
        @DisplayName("세션 매핑이 없으면 아무 작업도 하지 않는다")
        void doesNothingWhenSessionNotFound() {
            when(valueOps.get("chat:session:session-1")).thenReturn(null);

            service.disconnect("session-1");

            verify(setOps, never()).remove(any(), any());
        }
    }

    @Nested
    @DisplayName("getOnlineUserIds — 접속 중 유저 조회")
    class GetOnlineUserIds {

        @Test
        @DisplayName("채팅방 접속 중인 유저 ID 목록을 반환한다")
        void returnsOnlineUserIds() {
            when(setOps.members("chat:online:5")).thenReturn(Set.of("10", "20"));

            Set<Long> result = service.getOnlineUserIds(5L);

            assertThat(result).containsExactlyInAnyOrder(10L, 20L);
        }

        @Test
        @DisplayName("접속 중인 유저가 없으면 빈 셋을 반환한다")
        void returnsEmptySetWhenNoOneOnline() {
            when(setOps.members("chat:online:5")).thenReturn(Set.of());

            Set<Long> result = service.getOnlineUserIds(5L);

            assertThat(result).isEmpty();
        }
    }
}
