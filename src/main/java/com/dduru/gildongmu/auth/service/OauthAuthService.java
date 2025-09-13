package com.dduru.gildongmu.auth.service;

import com.dduru.gildongmu.auth.dto.LoginRequest;
import com.dduru.gildongmu.auth.dto.LoginResponse;
import com.dduru.gildongmu.auth.dto.OauthUserInfo;
import com.dduru.gildongmu.auth.dto.web.WebLoginRequest;
import com.dduru.gildongmu.auth.dto.web.WebOAuthUrlResponse;
import com.dduru.gildongmu.auth.exception.InvalidTokenException;
import com.dduru.gildongmu.auth.exception.RefreshTokenException;
import com.dduru.gildongmu.auth.exception.TokenRefreshFailedException;
import com.dduru.gildongmu.auth.exception.UserNotFoundException;
import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;
import com.dduru.gildongmu.common.jwt.JwtTokenProvider;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.enums.AgeRange;
import com.dduru.gildongmu.user.enums.Gender;
import com.dduru.gildongmu.user.enums.OauthType;
import com.dduru.gildongmu.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OauthAuthService {

    private final OauthFactory oauthFactory;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    public WebOAuthUrlResponse getAuthorizationUrl(String provider) {
        OauthService oauthService = oauthFactory.getOauthService(OauthType.fromValue(provider));
        String authUrl = oauthService.getAuthorizationUrl();
        return new WebOAuthUrlResponse(authUrl);
    }

    public LoginResponse processLogin(String provider, WebLoginRequest request) {
        requireNonBlank(request.code());
        String code = safeUrlDecode(request.code());

        OauthService oauthService = oauthFactory.getOauthService(OauthType.fromValue(provider));
        String accessToken = oauthService.getAccessToken(code);
        OauthUserInfo oauthUserInfo = oauthService.getUserInfo(accessToken);
        return createLoginResponse(oauthUserInfo);
    }

    public LoginResponse processTokenLogin(String provider, LoginRequest request) {
        OauthService oauthService = oauthFactory.getOauthService(OauthType.fromValue(provider));
        OauthUserInfo oauthUserInfo = oauthService.verifyIdToken(request.idToken());
        return createLoginResponse(oauthUserInfo);
    }

    private LoginResponse createLoginResponse(OauthUserInfo oauthUserInfo) {
        User user = findOrCreateUser(oauthUserInfo);
        String jwtToken = jwtTokenProvider.createToken(user.getId());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        refreshTokenService.saveRefreshToken(user.getId(), refreshToken);
        return LoginResponse.of(jwtToken, refreshToken);
    }

    public LoginResponse refreshAccessToken(String refreshToken) {
        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("사용자를 찾을 수 없음 - userId: {}", userId);
                    return new UserNotFoundException();
                });

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

        String newAccessToken = jwtTokenProvider.createToken(user.getId());

        extendTokenExpirationSafely(userId);    // refresh token 만료 기간 연장

        return LoginResponse.of(newAccessToken, refreshToken);
    }

    public void logout(Long userId) {

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

    private void deleteExpiredTokenSafely(Long userId) {
        try {
            refreshTokenService.deleteRefreshToken(userId);
        } catch (RefreshTokenException infraException) {
            log.warn("만료된 refresh token 삭제 실패 - userId: {}", userId, infraException);
        }
    }

    private void extendTokenExpirationSafely(Long userId) {
        try {
            refreshTokenService.refreshTokenExpiration(userId);
        } catch (RefreshTokenException infraException) {
            log.warn("Refresh token 만료 연장 실패 - userId: {}", userId, infraException);
        }
    }

    private User findOrCreateUser(OauthUserInfo oauthUserInfo) {
        Optional<User> existingUser = userRepository.findByOauthIdAndOauthType(
                oauthUserInfo.oauthId(),
                oauthUserInfo.loginType()
        );

        if (existingUser.isPresent()) {
            return existingUser.get();
        }

        if (userRepository.existsByEmail(oauthUserInfo.email())) {
            log.error("이미 존재하는 이메일로 다른 OAuth 제공자 가입 시도: {}", oauthUserInfo.email());
            throw new IllegalArgumentException("이미 다른 소셜 계정으로 가입된 이메일입니다: " + oauthUserInfo.email());
        }

        return createNewUser(oauthUserInfo);
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

    private void requireNonBlank(String code) {
        if (code == null || code.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "Authorization Code가 비어있습니다.");
        }
    }

    private String safeUrlDecode(String code) {
        try {
            return URLDecoder.decode(code, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return code;
        }
    }
}
