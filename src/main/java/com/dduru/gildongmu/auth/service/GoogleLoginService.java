package com.dduru.gildongmu.auth.service;

import com.dduru.gildongmu.auth.dto.OauthUserInfo;
import com.dduru.gildongmu.auth.dto.google.GoogleTokenResponse;
import com.dduru.gildongmu.auth.dto.google.GoogleUserResponse;
import com.dduru.gildongmu.auth.dto.google.PersonExtendedInfo;
import com.dduru.gildongmu.user.enums.OauthType;
import com.dduru.gildongmu.auth.utils.GoogleUserInfoExtractor;
import com.dduru.gildongmu.auth.utils.OauthConstants;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Arrays;
import java.util.Map;


@Slf4j
@Service
public class GoogleLoginService extends AbstractOauthService {

    @Value("${oauth.google.android-client-id}")
    private String googleAndroidClientId;

    @Value("${oauth.google.client-id}")
    private String googleClientId;

    @Value("${oauth.google.client-secret}")
    private String googleClientSecret;

    @Value("${oauth.google.redirect-uri}")
    private String googleRedirectUri;

    private final GoogleUserInfoExtractor userInfoExtractor;

    public GoogleLoginService(WebClient.Builder webClientBuilder, GoogleUserInfoExtractor userInfoExtractor) {
        super(webClientBuilder);
        this.userInfoExtractor = userInfoExtractor;
    }

    @Override
    public String getAuthorizationUrl() {
        return getAuthUrl() + "?" + buildUrlParams(
                "client_id", getClientId(),
                "redirect_uri", getRedirectUri(),
                "response_type", OauthConstants.Common.RESPONSE_TYPE_CODE,
                "scope", getScope(),
                "access_type", "offline"
        );
    }

    @Override
    public String getAccessToken(String code) {
        try {
            log.info("구글 액세스 토큰 요청 시작");

            String processedCode = (code != null && code.contains("%"))
                    ? java.net.URLDecoder.decode(code, java.nio.charset.StandardCharsets.UTF_8)
                    : code;

            GoogleTokenResponse response = webClient.post()
                    .uri(getTokenUrl())
                    .header(OauthConstants.Common.CONTENT_TYPE_HEADER, OauthConstants.Common.FORM_URLENCODED)
                    .bodyValue(buildTokenRequestBody(processedCode))
                    .retrieve()
                    .bodyToMono(GoogleTokenResponse.class)
                    .block();

            validateResponse(response, "구글 액세스 토큰 획득");

            if (response.accessToken() == null) {
                throw new RuntimeException("액세스 토큰이 null입니다.");
            }

            log.info("구글 액세스 토큰 획득 성공");
            return response.accessToken();

        } catch (Exception e) {
            handleOauthException(e, "구글 액세스 토큰 획득");
            return null;
        }
    }

    @Override
    public OauthUserInfo getUserInfo(String accessToken) {
        try {
            log.info("구글 사용자 정보 조회 시작");

            GoogleUserResponse basicInfo = getBasicUserInfo(accessToken);
            PersonExtendedInfo extendedInfo = getExtendedPersonInfo(accessToken);

            return buildOauthUserInfo(basicInfo, extendedInfo);

        } catch (Exception e) {
            handleOauthException(e, "구글 사용자 정보 조회");
            return null;
        }
    }

    @Override
    public OauthUserInfo verifyIdToken(String idToken) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance())
                    .setAudience(Arrays.asList(
                            getClientId(),
                            googleAndroidClientId))
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

    private GoogleUserResponse getBasicUserInfo(String accessToken) {
        GoogleUserResponse basicInfo = webClient.get()
                .uri(getUserInfoUrl())
                .header(OauthConstants.Common.AUTHORIZATION_HEADER,
                        OauthConstants.Common.BEARER_PREFIX + accessToken)
                .retrieve()
                .bodyToMono(GoogleUserResponse.class)
                .block();

        validateResponse(basicInfo, "구글 사용자 정보 조회");
        return basicInfo;
    }

    @SuppressWarnings("unchecked")
    private PersonExtendedInfo getExtendedPersonInfo(String accessToken) {
        try {
            log.info("People API 호출 시작");

            String uri = OauthConstants.Google.PEOPLE_API_URL + "?personFields=" + OauthConstants.Google.PERSON_FIELDS;

            Map<String, Object> response = webClient.get()
                    .uri(uri)
                    .header(OauthConstants.Common.AUTHORIZATION_HEADER,
                            OauthConstants.Common.BEARER_PREFIX + accessToken)
                    .retrieve()
                    .onStatus(
                            httpStatus -> httpStatus.is4xxClientError() || httpStatus.is5xxServerError(),
                            clientResponse -> {
                                log.error("People API 실패: HTTP {}", clientResponse.statusCode());
                                return clientResponse.bodyToMono(String.class)
                                        .doOnNext(errorBody -> log.error("People API 에러: {}", errorBody))
                                        .then(clientResponse.createException());
                            }
                    )
                    .bodyToMono(Map.class)
                    .block();

            if (response != null) {
                log.info("People API 성공 - 키: {}", response.keySet());
            }

            return userInfoExtractor.extractPersonInfo(response);

        } catch (WebClientResponseException e) {
            log.warn("People API 실패: HTTP {} - 기본 정보만 사용", e.getStatusCode());
            return PersonExtendedInfo.empty();
        } catch (Exception e) {
            log.warn("People API 예외: {} - 기본 정보만 사용", e.getMessage());
            return PersonExtendedInfo.empty();
        }
    }

    private OauthUserInfo buildOauthUserInfo(GoogleUserResponse basicInfo, PersonExtendedInfo extendedInfo) {
        return OauthUserInfo.builder()
                .oauthId(basicInfo.id())
                .email(basicInfo.email())
                .name(basicInfo.name())
                .profileImage(basicInfo.picture())
                .loginType(OauthType.GOOGLE)
                .gender(extendedInfo.gender())
                .ageRange(extendedInfo.ageRange())
                .phoneNumber(extendedInfo.phoneNumber())
                .build();
    }

    @Override
    protected String getClientId() { return googleClientId; }
    @Override
    protected String getClientSecret() { return googleClientSecret; }
    @Override
    protected String getRedirectUri() { return googleRedirectUri; }
    @Override
    protected String getAuthUrl() { return OauthConstants.Google.AUTH_URL; }
    @Override
    protected String getTokenUrl() { return OauthConstants.Google.TOKEN_URL; }
    @Override
    protected String getUserInfoUrl() { return OauthConstants.Google.USER_INFO_URL; }
    @Override
    protected String getScope() { return OauthConstants.Google.SCOPE; }
    @Override
    public OauthType getLoginType() { return OauthType.GOOGLE; }
}
