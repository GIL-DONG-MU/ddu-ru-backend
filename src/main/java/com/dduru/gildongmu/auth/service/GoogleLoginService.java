package com.dduru.gildongmu.auth.service;

import com.dduru.gildongmu.auth.dto.OauthUserInfo;
import com.dduru.gildongmu.user.enums.OauthType;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;


@Slf4j
@Service
public class GoogleLoginService extends AbstractOauthService {

    @Value("${oauth.google.android-client-id}")
    private String googleAndroidClientId;

    @Value("${oauth.google.client-id}")
    private String googleClientId;

    public GoogleLoginService(WebClient.Builder webClientBuilder) {
        super(webClientBuilder);
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
                    // 회원가입 시 기본 정보만 받으므로 추가 정보는 추출하지 않음
                    // .gender(null)
                    // .phoneNumber(null)
                    .build();

        } catch (Exception e) {
            handleOauthException(e, "구글 ID Token 검증");
            return null;
        }
    }

    @Override
    protected String getClientId() { return googleClientId; }
    @Override
    public OauthType getLoginType() { return OauthType.GOOGLE; }
}
