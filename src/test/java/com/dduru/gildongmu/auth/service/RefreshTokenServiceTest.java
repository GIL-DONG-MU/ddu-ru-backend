package com.dduru.gildongmu.auth.service;

import com.dduru.gildongmu.auth.repository.RefreshTokenRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RefreshTokenService 테스트")
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @Test
    @DisplayName("Refresh token 저장 성공")
    void saveRefreshToken_성공() {
        // given
        Long userId = 1L;
        String refreshToken = "refresh-token-value";

        // when
        refreshTokenService.saveRefreshToken(userId, refreshToken);

        // then
        verify(refreshTokenRepository).save(userId, refreshToken);
    }

    @Test
    @DisplayName("Refresh token 삭제 성공 시 true 반환")
    void deleteRefreshToken_성공_true반환() {
        // given
        Long userId = 1L;
        when(refreshTokenRepository.delete(userId)).thenReturn(true);

        // when
        boolean result = refreshTokenService.deleteRefreshToken(userId);

        // then
        assertThat(result).isTrue();
        verify(refreshTokenRepository).delete(userId);
    }

    @Test
    @DisplayName("Refresh token 삭제 시 키가 없으면 false 반환")
    void deleteRefreshToken_키없음_false반환() {
        // given
        Long userId = 1L;
        when(refreshTokenRepository.delete(userId)).thenReturn(false);

        // when
        boolean result = refreshTokenService.deleteRefreshToken(userId);

        // then
        assertThat(result).isFalse();
        verify(refreshTokenRepository).delete(userId);
    }

    @Test
    @DisplayName("저장된 토큰과 일치하면 validate true")
    void validateRefreshToken_일치_true반환() {
        // given
        Long userId = 1L;
        String refreshToken = "valid-refresh-token";
        when(refreshTokenRepository.findByUserId(userId)).thenReturn(Optional.of(refreshToken));

        // when
        boolean result = refreshTokenService.validateRefreshToken(userId, refreshToken);

        // then
        assertThat(result).isTrue();
        verify(refreshTokenRepository).findByUserId(userId);
    }

    @Test
    @DisplayName("저장된 토큰과 불일치하면 validate false")
    void validateRefreshToken_불일치_false반환() {
        // given
        Long userId = 1L;
        String requestToken = "wrong-token";
        when(refreshTokenRepository.findByUserId(userId)).thenReturn(Optional.of("stored-token"));

        // when
        boolean result = refreshTokenService.validateRefreshToken(userId, requestToken);

        // then
        assertThat(result).isFalse();
        verify(refreshTokenRepository).findByUserId(userId);
    }

    @Test
    @DisplayName("저장된 토큰이 없으면 validate false")
    void validateRefreshToken_토큰없음_false반환() {
        // given
        Long userId = 1L;
        String refreshToken = "any-token";
        when(refreshTokenRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // when
        boolean result = refreshTokenService.validateRefreshToken(userId, refreshToken);

        // then
        assertThat(result).isFalse();
        verify(refreshTokenRepository).findByUserId(userId);
    }

    @Test
    @DisplayName("refreshToken이 null이면 validate false (Repository 호출 없음)")
    void validateRefreshToken_null_false반환() {
        // given
        Long userId = 1L;

        // when
        boolean result = refreshTokenService.validateRefreshToken(userId, null);

        // then
        assertThat(result).isFalse();
        verify(refreshTokenRepository, never()).findByUserId(anyLong());
    }

    @Test
    @DisplayName("Refresh token 만료 연장 성공")
    void refreshTokenExpiration_성공() {
        // given
        Long userId = 1L;

        // when
        refreshTokenService.refreshTokenExpiration(userId);

        // then
        verify(refreshTokenRepository).extendExpiration(userId);
    }
}
