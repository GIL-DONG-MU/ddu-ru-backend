package com.dduru.gildongmu.auth.service;

import com.dduru.gildongmu.auth.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    public void saveRefreshToken(Long userId, String refreshToken) {
        refreshTokenRepository.save(userId, refreshToken);
        log.debug("Refresh token 저장 - userId: {}", userId);
    }

    public boolean deleteRefreshToken(Long userId) {
        boolean deleted = refreshTokenRepository.delete(userId);
        log.debug("Refresh token 삭제 - userId: {}, 성공: {}", userId, deleted);
        return deleted;
    }

    public boolean validateRefreshToken(Long userId, String refreshToken) {
        if (refreshToken == null) {
            return false;
        }
        return refreshTokenRepository.findByUserId(userId)
                .map(stored -> stored.equals(refreshToken))
                .orElse(false);
    }

    public void refreshTokenExpiration(Long userId) {
        refreshTokenRepository.extendExpiration(userId);
        log.debug("Refresh token 만료 연장 - userId: {}", userId);
    }
}
