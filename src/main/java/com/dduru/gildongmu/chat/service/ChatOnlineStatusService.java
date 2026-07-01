package com.dduru.gildongmu.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatOnlineStatusService {

    private static final String ONLINE_KEY_PREFIX = "chat:online:";
    private static final String SESSION_KEY_PREFIX = "chat:session:";
    private static final String SUBSCRIPTION_KEY_PREFIX = "chat:subscription:";
    private static final Duration PRESENCE_TTL = Duration.ofHours(2);

    private final RedisTemplate<String, String> redisTemplate;

    public void enter(Long roomId, Long userId, String sessionId, String subscriptionId) {
        String onlineKey = onlineKey(roomId);
        redisTemplate.opsForSet().add(onlineKey, userId.toString());
        redisTemplate.expire(onlineKey, PRESENCE_TTL); // add()는 TTL을 갱신하지 않아 매번 명시적으로 설정
        redisTemplate.opsForValue().set(sessionKey(sessionId), userId + ":" + roomId, PRESENCE_TTL);
        redisTemplate.opsForValue().set(subscriptionKey(sessionId, subscriptionId), roomId.toString(), PRESENCE_TTL);
    }

    // STOMP UNSUBSCRIBE 프레임은 destination 없이 subscriptionId만 전달 → subscription 키로 roomId 역추적
    public void leave(String sessionId, String subscriptionId, Long userId) {
        String subscriptionKey = subscriptionKey(sessionId, subscriptionId);
        String roomIdValue = redisTemplate.opsForValue().get(subscriptionKey);
        if (roomIdValue == null) return;
        try {
            Long roomId = Long.parseLong(roomIdValue);
            redisTemplate.opsForSet().remove(onlineKey(roomId), userId.toString());
        } catch (NumberFormatException e) {
            // 데이터 손상 시에도 subscription 키는 반드시 삭제
        } finally {
            redisTemplate.delete(subscriptionKey);
        }
    }

    // 앱 강제 종료 시 UNSUBSCRIBE 없이 연결이 끊김 → sessionId만 알 수 있어 세션 키로 roomId 역추적
    public void disconnect(String sessionId) {
        String sessionValue = redisTemplate.opsForValue().get(sessionKey(sessionId));
        if (sessionValue == null) return;

        String[] parts = sessionValue.split(":");
        if (parts.length != 2) return;

        String userId = parts[0];
        String roomId = parts[1];
        try {
            redisTemplate.opsForSet().remove(onlineKey(Long.parseLong(roomId)), userId);
        } catch (NumberFormatException e) {
            // 데이터 손상 시에도 세션 키는 반드시 삭제
        } finally {
            redisTemplate.delete(sessionKey(sessionId));
        }
    }

    public Set<Long> getOnlineUserIds(Long roomId) {
        Set<String> members = redisTemplate.opsForSet().members(onlineKey(roomId));
        if (members == null || members.isEmpty()) return Collections.emptySet();
        return members.stream().map(Long::parseLong).collect(Collectors.toSet());
    }

    private String onlineKey(Long roomId) {
        return ONLINE_KEY_PREFIX + roomId;
    }

    private String sessionKey(String sessionId) {
        return SESSION_KEY_PREFIX + sessionId;
    }

    private String subscriptionKey(String sessionId, String subscriptionId) {
        return SUBSCRIPTION_KEY_PREFIX + sessionId + ":" + subscriptionId;
    }
}
