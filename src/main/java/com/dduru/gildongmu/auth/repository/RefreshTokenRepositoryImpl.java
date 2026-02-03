package com.dduru.gildongmu.auth.repository;

import com.dduru.gildongmu.auth.exception.RefreshTokenException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class RefreshTokenRepositoryImpl implements RefreshTokenRepository {

    private static final String REFRESH_TOKEN_PREFIX = "refresh_token:";

    private final RedisTemplate<String, String> redisTemplate;

    @Value("${jwt.refresh-expiration}")
    private Duration refreshTtl;

    @Override
    public void save(Long userId, String refreshToken) {
        try {
            redisTemplate.opsForValue().set(key(userId), refreshToken, refreshTtl);
        } catch (Exception e) {
            log.error("Refresh token 저장 실패 - userId: {}", userId, e);
            throw new RefreshTokenException("Refresh token 저장 실패");
        }
    }

    @Override
    public Optional<String> findByUserId(Long userId) {
        try {
            return Optional.ofNullable(redisTemplate.opsForValue().get(key(userId)));
        } catch (Exception e) {
            log.error("Refresh token 조회 실패 - userId: {}", userId, e);
            throw new RefreshTokenException("Refresh token 조회 실패");
        }
    }

    @Override
    public boolean delete(Long userId) {
        try {
            return redisTemplate.delete(key(userId));
        } catch (Exception e) {
            log.error("Refresh token 삭제 실패 - userId: {}", userId, e);
            throw new RefreshTokenException("Refresh token 삭제 실패");
        }
    }

    @Override
    public void extendExpiration(Long userId) {
        try {
            Boolean extended = redisTemplate.expire(key(userId), refreshTtl);
            if (Boolean.FALSE.equals(extended)) {
                log.debug("Refresh token 키 없음 - 만료 연장 스킵, userId: {}", userId);
            }
        } catch (Exception e) {
            log.error("Refresh token 만료 연장 실패 - userId: {}", userId, e);
            throw new RefreshTokenException("Refresh token 만료 연장 실패");
        }
    }

    private String key(Long userId) {
        return REFRESH_TOKEN_PREFIX + userId;
    }
}
