package com.dduru.gildongmu.auth.service;

import com.dduru.gildongmu.auth.dto.OauthUserInfo;
import com.dduru.gildongmu.auth.dto.kakao.KakaoAccount;
import com.dduru.gildongmu.auth.dto.kakao.KakaoTokenResponse;
import com.dduru.gildongmu.auth.dto.kakao.KakaoUserResponse;
import com.dduru.gildongmu.user.enums.OauthType;
import com.dduru.gildongmu.auth.utils.OauthConstants;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Base64;
import java.util.Optional;

@Service
public class KakaoLoginService extends AbstractOauthService {

    @Value("${oauth.kakao.client-id}")
    private String kakaoClientId;

    @Value("${oauth.kakao.client-secret}")
    private String kakaoClientSecret;

    @Value("${oauth.kakao.redirect-uri}")
    private String kakaoRedirectUri;

    private final ObjectMapper objectMapper;
    private final KakaoUserInfoMapper userInfoMapper;

    public KakaoLoginService(WebClient webClient) {
        super(webClient);
        this.objectMapper = new ObjectMapper();
        this.userInfoMapper = new KakaoUserInfoMapper();
    }

    @Override
    public String getAuthorizationUrl() {
        return getAuthUrl() + "?" + buildUrlParams(
                "client_id", getClientId(),
                "redirect_uri", getRedirectUri(),
                "response_type", OauthConstants.Common.RESPONSE_TYPE_CODE,
                "scope", getScope()
        );
    }

    @Override
    public String getAccessToken(String code) {
        try {
            KakaoTokenResponse response = webClient.post()
                    .uri(getTokenUrl())
                    .header(OauthConstants.Common.CONTENT_TYPE_HEADER, OauthConstants.Common.FORM_URLENCODED)
                    .bodyValue(buildTokenRequestBody(code))
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
                    .header(OauthConstants.Common.AUTHORIZATION_HEADER,
                            OauthConstants.Common.BEARER_PREFIX + accessToken)
                    .retrieve()
                    .bodyToMono(KakaoUserResponse.class)
                    .block();

            validateResponse(response, "카카오 사용자 정보 조회");
            return userInfoMapper.mapToOauthUserInfo(response);
        } catch (Exception e) {
            handleOauthException(e, "카카오 사용자 정보 조회");
            return null;
        }
    }

    @Override
    public OauthUserInfo verifyIdToken(String idToken) {
        try {
            JsonNode payload = parseIdTokenPayload(idToken);
            validateAudience(payload);

            return userInfoMapper.mapFromIdToken(payload);

        } catch (Exception e) {
            handleOauthException(e, "카카오 ID Token 검증");
            return null;
        }
    }

    private JsonNode parseIdTokenPayload(String idToken) throws Exception {
        String[] chunks = idToken.split("\\.");
        if (chunks.length != 3) {
            throw new RuntimeException("잘못된 ID Token 형식입니다.");
        }

        Base64.Decoder decoder = Base64.getUrlDecoder();
        String payload = new String(decoder.decode(chunks[1]));
        return objectMapper.readTree(payload);
    }

    private void validateAudience(JsonNode payload) {
        if (!getClientId().equals(payload.get("aud").asText())) {
            throw new RuntimeException("잘못된 클라이언트 ID입니다.");
        }
    }

    private static class KakaoUserInfoMapper {

        public OauthUserInfo mapToOauthUserInfo(KakaoUserResponse response) {
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

        public OauthUserInfo mapFromIdToken(JsonNode payload) {
            return OauthUserInfo.builder()
                    .oauthId(payload.get("sub").asText())
                    .email(getJsonValue(payload, "email"))
                    .name(getJsonValue(payload, "nickname"))
                    .profileImage(getJsonValue(payload, "picture"))
                    .loginType(OauthType.KAKAO)
                    .gender(getJsonValue(payload, "gender"))
                    .ageRange(getJsonValue(payload, "age_range"))
                    .phoneNumber(getJsonValue(payload, "phone_number"))
                    .build();
        }

        private String getValueIfAgreed(String value, Boolean needsAgreement) {
            return (value != null && (needsAgreement == null || !needsAgreement)) ? value : null;
        }

        private String getJsonValue(JsonNode json, String key) {
            return Optional.ofNullable(json.get(key))
                    .filter(node -> !node.isNull())
                    .map(JsonNode::asText)
                    .orElse(null);
        }
    }

    @Override
    protected String getClientId() { return kakaoClientId; }
    @Override
    protected String getClientSecret() { return kakaoClientSecret; }
    @Override
    protected String getRedirectUri() { return kakaoRedirectUri; }
    @Override
    protected String getAuthUrl() { return OauthConstants.Kakao.AUTH_URL; }
    @Override
    protected String getTokenUrl() { return OauthConstants.Kakao.TOKEN_URL; }
    @Override
    protected String getUserInfoUrl() { return OauthConstants.Kakao.USER_INFO_URL; }
    @Override
    protected String getScope() { return OauthConstants.Kakao.SCOPE; }
    @Override
    public OauthType getLoginType() { return OauthType.KAKAO; }
}
