package com.dduru.gildongmu.auth.service;

import com.dduru.gildongmu.auth.domain.User;
import com.dduru.gildongmu.auth.dto.LoginResponse;
import com.dduru.gildongmu.auth.dto.OauthUserInfo;
import com.dduru.gildongmu.auth.enums.AgeRange;
import com.dduru.gildongmu.auth.enums.Gender;
import com.dduru.gildongmu.auth.repository.UserRepository;
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

        return LoginResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .name(user.getName())
                .email(user.getEmail())
                .profileImage(user.getProfileImage())
                .gender(getEnumName(user.getGender()))
                .ageRange(getEnumName(user.getAgeRange()))
                .phoneNumber(user.getPhoneNumber())
                .build();
    }

    public LoginResponse refreshAccessToken(String refreshToken, String userId) {
        try {
            log.info("Access Token 재발급 시작 - userId: {}", userId);

            if (!refreshTokenService.validateRefreshToken(userId, refreshToken)) {
                log.error("Redis에 저장된 refresh token과 일치하지 않음 - userId: {}", userId);
                throw new RuntimeException("유효하지 않은 refresh token입니다.");
            }

            if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
                log.error("JWT refresh token 검증 실패 - userId: {}", userId);
                refreshTokenService.deleteRefreshToken(userId);
                throw new RuntimeException("만료되거나 유효하지 않은 refresh token입니다.");
            }

            User user = userRepository.findById(Long.parseLong(userId))
                    .orElseThrow(() -> {
                        log.error("사용자를 찾을 수 없음 - userId: {}", userId);
                        return new RuntimeException("사용자를 찾을 수 없습니다.");
                    });

            String newAccessToken = jwtTokenProvider.createToken(user.getId().toString());

            refreshTokenService.refreshTokenExpiration(userId);

            log.info("Access Token 재발급 성공 - userId: {}", userId);

            return LoginResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(refreshToken)
                    .name(user.getName())
                    .email(user.getEmail())
                    .profileImage(user.getProfileImage())
                    .gender(getEnumName(user.getGender()))
                    .ageRange(getEnumName(user.getAgeRange()))
                    .phoneNumber(user.getPhoneNumber())
                    .build();

        } catch (Exception e) {
            log.error("Access Token 재발급 실패 - userId: {}, error: {}", userId, e.getMessage());
            throw e;
        }
    }

    public void logout(String userId) {
        try {
            refreshTokenService.deleteRefreshToken(userId);
            log.info("로그아웃 성공 - userId: {}", userId);
        } catch (Exception e) {
            log.error("로그아웃 실패 - userId: {}, error: {}", userId, e.getMessage());
            throw new RuntimeException("로그아웃 처리 중 오류가 발생했습니다.", e);
        }
    }

    private String getEnumName(Enum<?> enumValue) {
        return enumValue != null ? enumValue.name() : null;
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
