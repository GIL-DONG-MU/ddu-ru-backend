package com.dduru.gildongmu.auth.service;

import com.dduru.gildongmu.auth.dto.OauthUserInfo;
import com.dduru.gildongmu.auth.dto.google.GoogleTokenResponse;
import com.dduru.gildongmu.auth.dto.google.GoogleUserResponse;
import com.dduru.gildongmu.auth.enums.OauthType;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;

@Service
public class GoogleLoginService extends AbstractOauthService  {


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

    private static final String GOOGLE_SCOPE = "email profile";

    public GoogleLoginService(WebClient webClient) {
        super(webClient);
    }

    @Override
    public String getAuthorizationUrl() {
        return getAuthUrl() + "?" + buildUrlParams(
                "client_id", getClientId(),
                "redirect_uri", getRedirectUri(),
                "response_type", "code",
                "scope", getScope(),
                "access_type", "offline"
        );
    }

    @Override
    public String getAccessToken(String code) {
        try {
            String body = buildUrlParams(
                    "grant_type", "authorization_code",
                    "client_id", getClientId(),
                    "client_secret", getClientSecret(),
                    "code", code,
                    "redirect_uri", getRedirectUri()
            );

            GoogleTokenResponse response = webClient.post()
                    .uri(getTokenUrl())
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(GoogleTokenResponse.class)
                    .block();

            validateResponse(response, "구글 액세스 토큰 획득");

            if (response.accessToken() == null) {
                throw new RuntimeException("액세스 토큰이 null입니다.");
            }

            return response.accessToken();
        } catch (Exception e) {
            handleOauthException(e, "구글 액세스 토큰 획득");
            return null;
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

            validateResponse(response, "구글 사용자 정보 조회");

            return OauthUserInfo.builder()
                    .oauthId(response.id())
                    .email(response.email())
                    .name(response.name())
                    .profileImage(response.picture())
                    .loginType(OauthType.GOOGLE)
                    .build();
        } catch (Exception e) {
            handleOauthException(e, "구글 사용자 정보 조회");
            return null;
        }
    }

    public OauthUserInfo verifyIdToken(String idToken) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    new GsonFactory())
                    .setAudience(Collections.singletonList(getClientId()))
                    .build();

            GoogleIdToken token = verifier.verify(idToken);
            if (token == null) {
                throw new RuntimeException("유효하지 않은 ID Token입니다.");
            }

            GoogleIdToken.Payload payload = token.getPayload();

            return OauthUserInfo.builder()
                    .oauthId(payload.getSubject())
                    .email(payload.getEmail())
                    .name((String) payload.get("name"))
                    .profileImage((String) payload.get("picture"))
                    .loginType(OauthType.GOOGLE)
                    .build();

        } catch (Exception e) {
            handleOauthException(e, "구글 ID Token 검증");
            return null;
        }
    }

    @Override
    protected String getClientId() { return googleClientId; }

    @Override
    protected String getClientSecret() { return googleClientSecret; }

    @Override
    protected String getRedirectUri() { return googleRedirectUri; }

    @Override
    protected String getAuthUrl() { return googleAuthUrl; }

    @Override
    protected String getTokenUrl() { return googleTokenUrl; }

    @Override
    protected String getUserInfoUrl() { return googleUserInfoUrl; }

    @Override
    protected String getScope() { return GOOGLE_SCOPE; }

    @Override
    public OauthType getLoginType() {
        return OauthType.GOOGLE;
    }
}
