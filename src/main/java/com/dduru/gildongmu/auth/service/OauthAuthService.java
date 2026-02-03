package com.dduru.gildongmu.auth.service;

import com.dduru.gildongmu.auth.dto.LoginRequest;
import com.dduru.gildongmu.auth.dto.LoginResponse;
import com.dduru.gildongmu.auth.dto.OauthUserInfo;
import com.dduru.gildongmu.auth.dto.TokenPair;
import com.dduru.gildongmu.auth.dto.UserCreationResult;
import com.dduru.gildongmu.auth.exception.*;
import com.dduru.gildongmu.common.jwt.JwtTokenProvider;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.enums.OauthType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OauthAuthService {

    private final OauthFactory oauthFactory;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final UserDomainService userDomainService;

    public LoginResponse processTokenLogin(String provider, LoginRequest request) {
        log.debug("OAuth 로그인 시작 - provider: {}", provider);
        OauthService oauthService = oauthFactory.getOauthService(OauthType.fromValue(provider));
        OauthUserInfo oauthUserInfo = oauthService.verifyIdToken(request.idToken());

        UserCreationResult result = userDomainService.findOrCreateUser(oauthUserInfo);
        TokenPair tokens = generateTokens(result.user().getId());
        logLoginSuccess(result, oauthUserInfo);
        return LoginResponse.of(tokens.accessToken(), tokens.refreshToken(), result.isNewUser());
    }

    public LoginResponse refreshAccessToken(String refreshToken) {
        log.debug("Access Token 갱신 시작");
        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        User user = userDomainService.getUserOrThrow(userId);
        validateRefreshTokenForRefresh(userId, refreshToken);
        String newAccessToken = jwtTokenProvider.createToken(user.getId());
        extendTokenExpirationSafely(userId);
        log.debug("Access Token 갱신 완료 - userId: {}", userId);
        return LoginResponse.of(newAccessToken, refreshToken, false);
    }

    public void logout(Long userId) {
        log.debug("로그아웃 시작 - userId: {}", userId);
        try {
            boolean deleted = refreshTokenService.deleteRefreshToken(userId);
            if (deleted) {
                log.info("로그아웃 성공 - userId: {}", userId);
            } else {
                log.debug("로그아웃 처리 (토큰이 이미 없음) - userId: {}", userId);
            }
        } catch (RefreshTokenException e) {
            log.warn("로그아웃 중 Redis 오류 (사용자에게는 성공 처리) - userId: {}", userId, e);
        }
    }

    private TokenPair generateTokens(Long userId) {
        String accessToken = jwtTokenProvider.createToken(userId);
        String refreshToken = jwtTokenProvider.createRefreshToken(userId);
        refreshTokenService.saveRefreshToken(userId, refreshToken);
        return new TokenPair(accessToken, refreshToken);
    }

    private void validateRefreshTokenForRefresh(Long userId, String refreshToken) {
        validateStoredRefreshToken(userId, refreshToken);
        validateJwtRefreshToken(refreshToken, userId);
    }

    private void validateStoredRefreshToken(Long userId, String refreshToken) {
        try {
            boolean matches = refreshTokenService.validateRefreshToken(userId, refreshToken);
            if (!matches) {
                log.warn("저장된 refresh token과 불일치 - userId: {}", userId);
                throw new InvalidTokenException("유효하지 않은 refresh token입니다.");
            }
        } catch (RefreshTokenException e) {
            log.error("Redis 접근 실패로 토큰 검증 불가 - userId: {}", userId, e);
            throw new TokenRefreshFailedException("토큰 검증 중 서버 오류가 발생했습니다.");
        }
    }

    private void validateJwtRefreshToken(String refreshToken, Long userId) {
        if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
            log.warn("JWT refresh token 검증 실패 - userId: {}", userId);
            deleteExpiredTokenSafely(userId);
            throw new InvalidTokenException("만료되거나 유효하지 않은 refresh token입니다.");
        }
    }

    private void logLoginSuccess(UserCreationResult result, OauthUserInfo oauthUserInfo) {
        User user = result.user();
        if (result.isNewUser()) {
            log.info("신규 사용자 로그인 성공 - userId: {}, provider: {}, email: {}", 
                    user.getId(), oauthUserInfo.loginType(), oauthUserInfo.email());
        } else {
            log.info("기존 사용자 로그인 성공 - userId: {}, provider: {}", 
                    user.getId(), oauthUserInfo.loginType());
        }
    }

    private void deleteExpiredTokenSafely(Long userId) {
        try {
            refreshTokenService.deleteRefreshToken(userId);
        } catch (RefreshTokenException e) {
            log.warn("만료된 refresh token 삭제 실패 - userId: {}", userId, e);
        }
    }

    private void extendTokenExpirationSafely(Long userId) {
        try {
            refreshTokenService.refreshTokenExpiration(userId);
        } catch (RefreshTokenException e) {
            log.warn("Refresh token 만료 연장 실패 - userId: {}", userId, e);
        }
    }

}
