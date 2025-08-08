package com.dduru.gildongmu.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${jwt.refresh-expiration}")
    private Duration refreshTtl;

    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";

    public void saveRefreshToken(String userId, String refreshToken) {
        redisTemplate.opsForValue().set(key(userId), refreshToken, refreshTtl);
        log.info("Refresh token 저장 - userId: {}", userId);
    }

    public Optional<String> getRefreshToken(String userId) {
        String token = redisTemplate.opsForValue().get(key(userId));
        return Optional.ofNullable(token);
    }

    public boolean deleteRefreshToken(String userId) {
        Boolean deleted = redisTemplate.delete(key(userId));
        boolean isDeleted = Boolean.TRUE.equals(deleted);
        log.info("Refresh token 삭제 - userId: {}, 성공: {}", userId, isDeleted);
        return isDeleted;
    }

    public boolean validateRefreshToken(String userId, String refreshToken) {
        return getRefreshToken(userId)
                .map(refreshToken::equals)
                .orElse(false);
    }

    public void refreshTokenExpiration(String userId) {
        redisTemplate.expire(key(userId), refreshTtl);
        log.info("Refresh token 만료 연장 - userId: {}", userId);
    }

    private String key(String userId) {
        return REFRESH_TOKEN_PREFIX + userId;
    }
}
