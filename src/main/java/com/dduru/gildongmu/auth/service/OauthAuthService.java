package com.dduru.gildongmu.auth.service;

import com.dduru.gildongmu.auth.dto.LoginRequest;
import com.dduru.gildongmu.auth.dto.LoginResponse;
import com.dduru.gildongmu.auth.dto.OauthUserInfo;
import com.dduru.gildongmu.auth.exception.DuplicateEmailException;
import com.dduru.gildongmu.auth.exception.InvalidTokenException;
import com.dduru.gildongmu.auth.exception.RefreshTokenException;
import com.dduru.gildongmu.auth.exception.TokenRefreshFailedException;
import com.dduru.gildongmu.auth.exception.UserNotFoundException;
import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;
import com.dduru.gildongmu.common.jwt.JwtTokenProvider;
import com.dduru.gildongmu.profile.domain.Profile;
import com.dduru.gildongmu.profile.repository.ProfileRepository;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.enums.OauthType;
import com.dduru.gildongmu.user.enums.Role;
import com.dduru.gildongmu.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class OauthAuthService {

    private final OauthFactory oauthFactory;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    public LoginResponse processTokenLogin(String provider, LoginRequest request) {
        OauthService oauthService = oauthFactory.getOauthService(OauthType.fromValue(provider));
        OauthUserInfo oauthUserInfo = oauthService.verifyIdToken(request.idToken());

        if (oauthUserInfo == null) {
            log.error("OAuth 사용자 정보 추출 실패 - provider: {}", provider);
            throw new BusinessException(ErrorCode.SOCIAL_LOGIN_FAILED, "사용자 정보를 가져올 수 없습니다.");
        }

        return createLoginResponse(oauthUserInfo);
    }

    private LoginResponse createLoginResponse(OauthUserInfo oauthUserInfo) {
        UserCreationResult result = findOrCreateUser(oauthUserInfo);
        User user = result.user();
        boolean isNewUser = result.isNewUser();

        String jwtToken = jwtTokenProvider.createToken(user.getId());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

        refreshTokenService.saveRefreshToken(user.getId(), refreshToken);
        return LoginResponse.of(jwtToken, refreshToken, isNewUser);
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

        extendTokenExpirationSafely(userId);

        return LoginResponse.of(newAccessToken, refreshToken, false);
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

    private UserCreationResult findOrCreateUser(OauthUserInfo oauthUserInfo) {
        Optional<User> existingUser = userRepository.findByOauthIdAndOauthType(
                oauthUserInfo.oauthId(),
                oauthUserInfo.loginType()
        );

        if (existingUser.isPresent()) {
            return new UserCreationResult(existingUser.get(), false);
        }

        if (userRepository.existsByEmail(oauthUserInfo.email())) {
            log.error("이미 존재하는 이메일로 다른 OAuth 제공자 가입 시도: {}", oauthUserInfo.email());
            throw DuplicateEmailException.of(oauthUserInfo.email());
        }

        User newUser = createNewUser(oauthUserInfo);
        return new UserCreationResult(newUser, true);
    }

    private record UserCreationResult(User user, boolean isNewUser) {
    }

    private User createNewUser(OauthUserInfo oauthUserInfo) {
        User newUser = User.builder()
                .email(oauthUserInfo.email())
                .name(oauthUserInfo.name())
                .oauthId(oauthUserInfo.oauthId())
                .oauthType(oauthUserInfo.loginType())
                .role(Role.USER)
                .build();

        User savedUser = userRepository.save(newUser);

        Profile profile = Profile.builder()
                .user(savedUser)
                .nickname(null)
                .gender(null)
                .phoneNumber(null)
                .birthday(null)
                .avatar(null)
                .bgColor(null)
                .uploadedImageUrl(null)
                .profileImageType(null)
                .bio(null)
                .build();

        profileRepository.save(profile);

        return savedUser;
    }
}
