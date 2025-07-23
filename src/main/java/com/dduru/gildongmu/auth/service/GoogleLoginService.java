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

    private static final String GOOGLE_AUTH_URL = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String GOOGLE_USER_INFO_URL = "https://www.googleapis.com/oauth2/v2/userinfo";

    @Override
    public String getAuthorizationUrl() {
        return GOOGLE_AUTH_URL + "?" +
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
                    .uri(GOOGLE_TOKEN_URL)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .bodyValue("grant_type=authorization_code" +
                            "&client_id=" + googleClientId +
                            "&client_secret=" + googleClientSecret +
                            "&code=" + code +
                            "&redirect_uri=" + googleRedirectUri)
                    .retrieve()
                    .bodyToMono(GoogleTokenResponse.class)
                    .block();

            if (response == null || response.getAccessToken() == null) {
                throw new BusinessException(ErrorCode.SOCIAL_LOGIN_FAILED, "구글 액세스 토큰 응답이 null입니다.");
            }

            return response.getAccessToken();
        } catch (Exception e) {
            log.error("구글 액세스 토큰 획득 실패", e);
            throw new BusinessException(ErrorCode.SOCIAL_LOGIN_FAILED, "구글 액세스 토큰 획득 중 예외 발생");
        }
    }

    @Override
    public OauthUserInfo getUserInfo(String accessToken) {
        try {
            GoogleUserResponse response = webClient.get()
                    .uri(GOOGLE_USER_INFO_URL)
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(GoogleUserResponse.class)
                    .block();

            if (response == null) {
                throw new BusinessException(ErrorCode.SOCIAL_LOGIN_FAILED, "구글 사용자 정보가 null입니다.");
            }

            return OauthUserInfo.builder()
                    .oauthId(response.getId())
                    .email(response.getEmail())
                    .name(response.getName())
                    .profileImage(response.getPicture())
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
