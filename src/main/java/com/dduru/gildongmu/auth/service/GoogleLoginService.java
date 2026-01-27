package com.dduru.gildongmu.auth.service;

import com.dduru.gildongmu.auth.dto.OauthUserInfo;
import com.dduru.gildongmu.auth.exception.InvalidTokenException;
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
                throw new InvalidTokenException("유효하지 않은 구글 ID Token입니다.");
            }

            GoogleIdToken.Payload payload = token.getPayload();

            return OauthUserInfo.builder()
                    .oauthId(payload.getSubject())
                    .email(payload.getEmail())
                    .name((String) payload.get("name"))
                    .loginType(OauthType.GOOGLE)
                    // 회원가입 시 기본 정보만 받으므로 추가 정보는 추출하지 않음
                    // .profileImage((String) payload.get("picture"))
                    // .gender(null)
                    // .phoneNumber(null)
                    .build();

        } catch (IllegalArgumentException e) {
            log.warn("구글 ID Token 형식 오류: {}", e.getMessage(), e);
            throw new InvalidTokenException("잘못된 구글 ID Token 형식입니다.");
        } catch (Exception e) {
            log.error("구글 ID Token 검증 중 예상치 못한 오류 발생", e);
            handleOauthException(e, "구글 ID Token 검증");
            throw new AssertionError("handleOauthException이 예외를 throw해야 하는데 throw하지 않았습니다.");
        }
    }

    @Override
    protected String getClientId() { return googleClientId; }
    @Override
    public OauthType getLoginType() { return OauthType.GOOGLE; }
}
