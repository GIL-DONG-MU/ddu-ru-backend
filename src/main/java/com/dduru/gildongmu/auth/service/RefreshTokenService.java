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

    @Value("${jwt.refresh-expiration:2592000000}")
    private long refreshTokenExpireTime;

    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";

    public void saveRefreshToken(String userId, String refreshToken) {
        try {
            String key = REFRESH_TOKEN_PREFIX + userId;
            long expireTimeInSeconds = refreshTokenExpireTime / 1000;
            redisTemplate.opsForValue().set(key, refreshToken, Duration.ofSeconds(expireTimeInSeconds));
            log.info("Refresh token 저장 성공 - userId: {}", userId);
        } catch (Exception e) {
            log.error("Refresh token 저장 실패 - userId: {}, error: {}", userId, e.getMessage());
            throw new RuntimeException("Refresh token 저장에 실패했습니다.", e);
        }
    }

    public Optional<String> getRefreshToken(String userId) {
        try {
            String key = REFRESH_TOKEN_PREFIX + userId;
            String refreshToken = redisTemplate.opsForValue().get(key);
            return Optional.ofNullable(refreshToken);
        } catch (Exception e) {
            log.error("Refresh token 조회 실패 - userId: {}, error: {}", userId, e.getMessage());
            return Optional.empty();
        }
    }

    public boolean deleteRefreshToken(String userId) {
        try {
            String key = REFRESH_TOKEN_PREFIX + userId;
            Boolean deleted = redisTemplate.delete(key);
            log.info("Refresh token 삭제 - userId: {}, 성공: {}", userId, deleted);
            return Boolean.TRUE.equals(deleted);
        } catch (Exception e) {
            log.error("Refresh token 삭제 실패 - userId: {}, error: {}", userId, e.getMessage());
            return false;
        }
    }

    public boolean validateRefreshToken(String userId, String refreshToken) {
        try {
            Optional<String> storedToken = getRefreshToken(userId);
            return storedToken.isPresent() && storedToken.get().equals(refreshToken);
        } catch (Exception e) {
            log.error("Refresh token 검증 실패 - userId: {}, error: {}", userId, e.getMessage());
            return false;
        }
    }

    public void refreshTokenExpiration(String userId) {
        try {
            String key = REFRESH_TOKEN_PREFIX + userId;
            long expireTimeInSeconds = refreshTokenExpireTime / 1000;
            redisTemplate.expire(key, Duration.ofSeconds(expireTimeInSeconds));
            log.info("Refresh token 만료시간 갱신 - userId: {}", userId);
        } catch (Exception e) {
            log.error("Refresh token 만료시간 갱신 실패 - userId: {}, error: {}", userId, e.getMessage());
        }
    }
}
