package com.dduru.gildongmu.auth.service;

import com.dduru.gildongmu.auth.dto.request.LoginRequest;
import com.dduru.gildongmu.auth.dto.response.LoginResponse;
import com.dduru.gildongmu.auth.dto.OauthUserInfo;
import com.dduru.gildongmu.auth.dto.TokenPair;
import com.dduru.gildongmu.auth.dto.UserCreationResult;
import com.dduru.gildongmu.auth.exception.*;
import com.dduru.gildongmu.common.exception.InternalServerException;
import com.dduru.gildongmu.common.jwt.JwtTokenProvider;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.domain.enums.OauthType;
import com.dduru.gildongmu.user.domain.enums.Role;
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
        OauthService oauthService = oauthFactory.getOauthService(OauthType.fromValue(provider));
        OauthUserInfo oauthUserInfo = oauthService.verifyIdToken(request.idToken());

        UserCreationResult result = userDomainService.findOrCreateUser(oauthUserInfo);
        TokenPair tokens = generateTokens(result.user());
        logLoginSuccess(result, oauthUserInfo);
        return LoginResponse.of(tokens.accessToken(), tokens.refreshToken(), result.isNewUser());
    }

    public LoginResponse refreshAccessToken(String refreshToken) {
        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        User user = userDomainService.getUserOrThrow(userId);
        validateRefreshTokenForRefresh(userId, refreshToken);
        String newAccessToken = jwtTokenProvider.createToken(user.getId(), user.getRole());
        extendTokenExpirationSafely(userId);
        return LoginResponse.of(newAccessToken, refreshToken, false);
    }

    public void logout(Long userId) {
        try {
            boolean deleted = refreshTokenService.deleteRefreshToken(userId);
            if (deleted) {
                log.info("로그아웃 성공 - userId: {}", userId);
            }
        } catch (InternalServerException e) {
            log.warn("로그아웃 중 Redis 오류 (사용자에게는 성공 처리) - userId: {}", userId, e);
        }
    }

    private TokenPair generateTokens(User user) {
        Long userId = user.getId();
        Role role = user.getRole();
        String accessToken = jwtTokenProvider.createToken(userId, role);
        String refreshToken = jwtTokenProvider.createRefreshToken(userId);
        try {
            refreshTokenService.saveRefreshToken(userId, refreshToken);
        } catch (InternalServerException e) {
            log.error("로그인 중 Refresh token 저장 실패 - userId: {}, 사용자에게는 로그인 성공 처리", userId, e);
        }
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
                throw new InvalidTokenException();
            }
        } catch (InternalServerException e) {
            log.error("Redis 접근 실패로 토큰 검증 불가 - userId: {}", userId, e);
            throw new InternalServerException();
        }
    }

    private void validateJwtRefreshToken(String refreshToken, Long userId) {
        if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
            log.warn("JWT refresh token 검증 실패 - userId: {}", userId);
            deleteExpiredTokenSafely(userId);
            throw new InvalidTokenException();
        }
    }

    private void logLoginSuccess(UserCreationResult result, OauthUserInfo oauthUserInfo) {
        if (result.isNewUser()) {
            return;
        }
        User user = result.user();
        log.info("기존 사용자 로그인 성공 - userId: {}, provider: {}",
                user.getId(), oauthUserInfo.loginType());
    }

    private void deleteExpiredTokenSafely(Long userId) {
        try {
            refreshTokenService.deleteRefreshToken(userId);
        } catch (InternalServerException e) {
            log.warn("만료된 refresh token 삭제 실패 - userId: {}", userId, e);
        }
    }

    private void extendTokenExpirationSafely(Long userId) {
        try {
            refreshTokenService.refreshTokenExpiration(userId);
        } catch (InternalServerException e) {
            log.warn("Refresh token 만료 연장 실패 - userId: {}", userId, e);
        }
    }

}
