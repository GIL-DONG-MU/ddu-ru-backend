package com.dduru.gildongmu.auth.service;

import com.dduru.gildongmu.auth.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    public void saveRefreshToken(Long userId, String refreshToken) {
        refreshTokenRepository.save(userId, refreshToken);
    }

    public boolean deleteRefreshToken(Long userId) {
        return refreshTokenRepository.delete(userId);
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
    }
}
