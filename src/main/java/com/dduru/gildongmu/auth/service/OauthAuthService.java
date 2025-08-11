package com.dduru.gildongmu.auth.service;

import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.auth.dto.LoginResponse;
import com.dduru.gildongmu.auth.dto.OauthUserInfo;
import com.dduru.gildongmu.user.enums.AgeRange;
import com.dduru.gildongmu.user.enums.Gender;
import com.dduru.gildongmu.auth.exception.*;
import com.dduru.gildongmu.user.repository.UserRepository;
import com.dduru.gildongmu.common.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OauthAuthService {

    private final OauthFactory oauthFactory;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    public String getAuthorizationUrl(String provider) {
        OauthService oauthService = oauthFactory.getOauthService(provider);
        return oauthService.getAuthorizationUrl();
    }

    public LoginResponse processLogin(String provider, String code) {
        OauthService oauthService = oauthFactory.getOauthService(provider);
        String accessToken = oauthService.getAccessToken(code);
        OauthUserInfo oauthUserInfo = oauthService.getUserInfo(accessToken);
        return createLoginResponse(oauthUserInfo);
    }

    public LoginResponse processTokenLogin(String provider, String idToken) {
        OauthService oauthService = oauthFactory.getOauthService(provider);
        OauthUserInfo oauthUserInfo = oauthService.verifyIdToken(idToken);
        return createLoginResponse(oauthUserInfo);
    }

    private LoginResponse createLoginResponse(OauthUserInfo oauthUserInfo) {
        User user = findOrCreateUser(oauthUserInfo);
        String jwtToken = jwtTokenProvider.createToken(user.getId().toString());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId().toString());

        refreshTokenService.saveRefreshToken(user.getId().toString(), refreshToken);
        return LoginResponse.of(jwtToken, refreshToken);
    }

    public LoginResponse refreshAccessToken(String refreshToken, String userId) {
        Long uid = parseUserId(userId);

        final boolean matches;
        try {
            matches = refreshTokenService.validateRefreshToken(userId, refreshToken);
        } catch (RefreshTokenException infraException) {
            log.error("Redis 접근 실패로 토큰 검증 불가 - userId: {}", userId, infraException);
            throw new TokenRefreshFailedException("토큰 검증 중 서버 오류가 발생했습니다.");
        }

        if (!matches) {
            log.warn("저장된 refresh token과 불일치 - userId: {}", userId);
            throw new InvalidTokenException("유효하지 않은 refresh token입니다.");
        }

        if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
            log.warn("JWT refresh token 검증 실패 - userId: {}", userId);
            deleteExpiredTokenSafely(userId);
            throw new InvalidTokenException("만료되거나 유효하지 않은 refresh token입니다.");
        }

        User user = userRepository.findById(uid)
                .orElseThrow(() -> {
                    log.error("사용자를 찾을 수 없음 - userId: {}", userId);
                    return new UserNotFoundException();
                });

        String newAccessToken = jwtTokenProvider.createToken(user.getId().toString());

        extendTokenExpirationSafely(userId);

        log.info("Access Token 재발급 성공 - userId: {}", userId);
        return LoginResponse.of(newAccessToken, refreshToken);
    }

    public void logout(String userId) {
        try {
            boolean deleted = refreshTokenService.deleteRefreshToken(userId);
            if (deleted) {
                log.info("로그아웃 성공 - userId: {}", userId);
            } else {
                log.info("로그아웃 처리 (토큰이 이미 없음) - userId: {}", userId);
            }
        } catch (RefreshTokenException infraException) {
            log.warn("로그아웃 중 Redis 오류 (사용자에게는 성공 처리) - userId: {}", userId, infraException);
        }
    }

    private void deleteExpiredTokenSafely(String userId) {
        try {
            refreshTokenService.deleteRefreshToken(userId);
        } catch (RefreshTokenException infraException) {
            log.warn("만료된 refresh token 삭제 실패 - userId: {}", userId, infraException);
        }
    }

    private void extendTokenExpirationSafely(String userId) {
        try {
            refreshTokenService.refreshTokenExpiration(userId);
        } catch (RefreshTokenException infraException) {
            log.warn("Refresh token 만료 연장 실패 - userId: {}", userId, infraException);
        }
    }

    private Long parseUserId(String userId) {
        try {
            return Long.parseLong(userId);
        } catch (NumberFormatException e) {
            log.warn("잘못된 사용자 ID 형식 - userId: {}", userId);
            throw new InvalidTokenException("잘못된 사용자 ID 형식입니다.");
        }
    }

    private User findOrCreateUser(OauthUserInfo oauthUserInfo) {
        return userRepository.findByOauthIdAndOauthType(
                oauthUserInfo.oauthId(),
                oauthUserInfo.loginType()
        ).orElseGet(() -> createNewUser(oauthUserInfo));
    }

    private User createNewUser(OauthUserInfo oauthUserInfo) {
        Gender gender = Gender.from(oauthUserInfo.gender());
        AgeRange ageRange = AgeRange.from(oauthUserInfo.ageRange());

        User newUser = User.builder()
                .email(oauthUserInfo.email())
                .name(oauthUserInfo.name())
                .nickname(oauthUserInfo.name())
                .profileImage(oauthUserInfo.profileImage())
                .oauthId(oauthUserInfo.oauthId())
                .oauthType(oauthUserInfo.loginType())
                .gender(gender)
                .ageRange(ageRange)
                .phoneNumber(oauthUserInfo.phoneNumber())
                .build();

        return userRepository.save(newUser);
    }
}
