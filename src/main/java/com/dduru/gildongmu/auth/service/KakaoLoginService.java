package com.dduru.gildongmu.auth.service;

import com.dduru.gildongmu.auth.dto.OauthUserInfo;
import com.dduru.gildongmu.auth.dto.kakao.KakaoAccount;
import com.dduru.gildongmu.auth.dto.kakao.KakaoTokenResponse;
import com.dduru.gildongmu.auth.dto.kakao.KakaoUserResponse;
import com.dduru.gildongmu.auth.enums.OauthType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Base64;

@Service
public class KakaoLoginService extends AbstractOauthService {


    @Value("${oauth.kakao.client-id}")
    private String kakaoClientId;

    @Value("${oauth.kakao.client-secret}")
    private String kakaoClientSecret;

    @Value("${oauth.kakao.redirect-uri}")
    private String kakaoRedirectUri;

    private static final String KAKAO_AUTH_URL = "https://kauth.kakao.com/oauth/authorize";
    private static final String KAKAO_TOKEN_URL = "https://kauth.kakao.com/oauth/token";
    private static final String KAKAO_USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";
    private static final String KAKAO_SCOPE = "openid,profile_nickname,profile_image,account_email,gender,age_range,phone_number";

    public KakaoLoginService(WebClient webClient) {
        super(webClient);
    }

    @Override
    public String getAuthorizationUrl() {
        return getAuthUrl() + "?" + buildUrlParams(
                "client_id", getClientId(),
                "redirect_uri", getRedirectUri(),
                "response_type", "code",
                "scope", getScope()
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

            KakaoTokenResponse response = webClient.post()
                    .uri(getTokenUrl())
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(KakaoTokenResponse.class)
                    .block();

            validateResponse(response, "카카오 액세스 토큰 획득");

            if (response.accessToken() == null) {
                throw new RuntimeException("액세스 토큰이 null입니다.");
            }

            return response.accessToken();
        } catch (Exception e) {
            handleOauthException(e, "카카오 액세스 토큰 획득");
            return null;
        }
    }

    @Override
    public OauthUserInfo getUserInfo(String accessToken) {
        try {
            KakaoUserResponse response = webClient.get()
                    .uri(getUserInfoUrl())
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(KakaoUserResponse.class)
                    .block();

            validateResponse(response, "카카오 사용자 정보 조회");
            return mapToOauthUserInfo(response);
        } catch (Exception e) {
            handleOauthException(e, "카카오 사용자 정보 조회");
            return null;
        }
    }

    @Override
    public OauthUserInfo verifyIdToken(String idToken) {
        try {
            String[] chunks = idToken.split("\\.");
            if (chunks.length != 3) {
                throw new RuntimeException("잘못된 ID Token 형식입니다.");
            }

            Base64.Decoder decoder = Base64.getUrlDecoder();
            String payload = new String(decoder.decode(chunks[1]));

            ObjectMapper mapper = new ObjectMapper();
            JsonNode payloadJson = mapper.readTree(payload);

            if (!getClientId().equals(payloadJson.get("aud").asText())) {
                throw new RuntimeException("잘못된 클라이언트 ID입니다.");
            }

            return OauthUserInfo.builder()
                    .oauthId(payloadJson.get("sub").asText())
                    .email(getJsonValue(payloadJson, "email"))
                    .name(getJsonValue(payloadJson, "nickname"))
                    .profileImage(getJsonValue(payloadJson, "picture"))
                    .loginType(OauthType.KAKAO)
                    .gender(getJsonValue(payloadJson, "gender"))
                    .ageRange(getJsonValue(payloadJson, "age_range"))
                    .phoneNumber(getJsonValue(payloadJson, "phone_number"))
                    .build();

        } catch (Exception e) {
            handleOauthException(e, "카카오 ID Token 검증");
            return null;
        }
    }

    private OauthUserInfo mapToOauthUserInfo(KakaoUserResponse response) {
        KakaoAccount account = response.kakaoAccount();

        return OauthUserInfo.builder()
                .oauthId(String.valueOf(response.id()))
                .email(account.email())
                .name(account.profile().nickname())
                .profileImage(account.profile().profileImageUrl())
                .loginType(OauthType.KAKAO)
                .gender(getValueIfAgreed(account.gender(), account.genderNeedsAgreement()))
                .ageRange(getValueIfAgreed(account.ageRange(), account.ageRangeNeedsAgreement()))
                .phoneNumber(getValueIfAgreed(account.phoneNumber(), account.phoneNumberNeedsAgreement()))
                .build();
    }

    private String getValueIfAgreed(String value, Boolean needsAgreement) {
        return (value != null && (needsAgreement == null || !needsAgreement)) ? value : null;
    }

    private String getJsonValue(JsonNode json, String key) {
        JsonNode node = json.get(key);
        return (node != null && !node.isNull()) ? node.asText() : null;
    }

    @Override
    protected String getClientId() { return kakaoClientId; }

    @Override
    protected String getClientSecret() { return kakaoClientSecret; }

    @Override
    protected String getRedirectUri() { return kakaoRedirectUri; }

    @Override
    protected String getAuthUrl() { return KAKAO_AUTH_URL; }

    @Override
    protected String getTokenUrl() { return KAKAO_TOKEN_URL; }

    @Override
    protected String getUserInfoUrl() { return KAKAO_USER_INFO_URL; }

    @Override
    protected String getScope() { return KAKAO_SCOPE; }

    @Override
    public OauthType getLoginType() {
        return OauthType.KAKAO;
    }
}
