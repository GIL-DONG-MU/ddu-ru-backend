package com.dduru.gildongmu.auth.service;

import com.dduru.gildongmu.auth.dto.OauthUserInfo;
import com.dduru.gildongmu.auth.dto.google.GoogleTokenResponse;
import com.dduru.gildongmu.auth.dto.google.GoogleUserResponse;
import com.dduru.gildongmu.auth.enums.OauthType;
import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoogleLoginService implements OauthService {

    private final WebClient webClient;

    @Value("${oauth.google.client-id}")
    private String googleClientId;

    @Value("${oauth.google.client-secret}")
    private String googleClientSecret;

    @Value("${oauth.google.redirect-uri}")
    private String googleRedirectUri;

    @Value("${oauth.google.auth-url}")
    private String googleAuthUrl;

    @Value("${oauth.google.token-url}")
    private String googleTokenUrl;

    @Value("${oauth.google.user-info-url}")
    private String googleUserInfoUrl;

    @Override
    public String getAuthorizationUrl() {
        return googleAuthUrl  + "?" +
                "client_id=" + googleClientId +
                "&redirect_uri=" + googleRedirectUri +
                "&response_type=code" +
                "&scope=email profile"+
                "&access_type=offline";
    }

    @Override
    public String getAccessToken(String code) {
        try {
            GoogleTokenResponse response = webClient.post()
                    .uri(googleTokenUrl)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .bodyValue("grant_type=authorization_code" +
                            "&client_id=" + googleClientId +
                            "&client_secret=" + googleClientSecret +
                            "&code=" + code +
                            "&redirect_uri=" + googleRedirectUri)
                    .retrieve()
                    .bodyToMono(GoogleTokenResponse.class)
                    .block();

            if (response == null || response.accessToken() == null) {
                log.error("구글 액세스 토큰 응답이 null입니다. code: {}", code);
                throw new BusinessException(ErrorCode.SOCIAL_LOGIN_FAILED, "소셜 로그인에 실패했습니다. 다시 시도해주세요.");
            }

            return response.accessToken();
        } catch (Exception e) {
            log.error("구글 액세스 토큰 획득 실패. code: {}", code, e);
            throw new BusinessException(ErrorCode.SOCIAL_LOGIN_FAILED, "소셜 로그인에 실패했습니다. 다시 시도해주세요.");
        }
    }

    @Override
    public OauthUserInfo getUserInfo(String accessToken) {
        try {
            GoogleUserResponse response = webClient.get()
                    .uri(googleUserInfoUrl)
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(GoogleUserResponse.class)
                    .block();

            if (response == null) {
                log.error("구글 사용자 정보 응답이 null입니다. accessToken: {}", accessToken);
                throw new BusinessException(ErrorCode.SOCIAL_LOGIN_FAILED, "소셜 로그인에 실패했습니다.");
            }

            return OauthUserInfo.builder()
                    .oauthId(response.id())
                    .email(response.email())
                    .name(response.name())
                    .profileImage(response.picture())
                    .loginType(OauthType.GOOGLE)
                    .gender(null)
                    .ageRange(null)
                    .phoneNumber(null)
                    .build();
        } catch (Exception e) {
            log.error("구글 사용자 정보 조회 실패", e);
            throw new BusinessException(ErrorCode.SOCIAL_LOGIN_FAILED, "구글 사용자 정보 조회 중 예외 발생");
        }
    }

    @Override
    public OauthType getLoginType() {
        return OauthType.GOOGLE;
    }
}
