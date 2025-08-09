package com.dduru.gildongmu.auth.service;

import com.dduru.gildongmu.auth.exception.RefreshTokenException;
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
        try {
            redisTemplate.opsForValue()
                    .set(key(userId), refreshToken,refreshTtl);
            log.info("Refresh token 저장 - userId: {}", userId);
        } catch (Exception e) {
            log.error("Refresh token 저장 실패 - userId: {}", userId, e);
            throw new RefreshTokenException("Refresh token 저장 실패");
        }
    }

    public Optional<String> getRefreshToken(String userId) {
        try {
            return Optional.ofNullable(redisTemplate.opsForValue().get(key(userId)));
        } catch (Exception e) {
            log.error("Refresh token 조회 실패 - userId: {}", userId, e);
            throw new RefreshTokenException("Refresh token 조회 실패");
        }
    }

    public boolean deleteRefreshToken(String userId) {
        try {
            boolean deleted = Boolean.TRUE.equals(redisTemplate.delete(key(userId)));
            log.info("Refresh token 삭제 - userId: {}, 성공: {}", userId, deleted);
            return deleted;
        } catch (Exception e) {
            log.error("Refresh token 삭제 실패 - userId: {}", userId, e);
            throw new RefreshTokenException("Refresh token 삭제 실패");
        }
    }

    public boolean validateRefreshToken(String userId, String refreshToken) {
        return getRefreshToken(userId)
                .map(refreshToken::equals)
                .orElse(false);
    }

    public void refreshTokenExpiration(String userId) {
        try {
            redisTemplate.expire(key(userId), refreshTtl);
            log.info("Refresh token 만료 연장 - userId: {}", userId);
        } catch (Exception e) {
            log.error("Refresh token 만료 연장 실패 - userId: {}", userId, e);
            throw new RefreshTokenException("Refresh token 만료 연장 실패");
        }
    }

    private String key(String userId) {
        return REFRESH_TOKEN_PREFIX + userId;
    }
}
