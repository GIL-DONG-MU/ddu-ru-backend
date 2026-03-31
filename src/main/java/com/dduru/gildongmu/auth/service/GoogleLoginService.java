package com.dduru.gildongmu.auth.service;

import com.dduru.gildongmu.auth.dto.OauthUserInfo;
import com.dduru.gildongmu.auth.exception.InvalidTokenException;
import com.dduru.gildongmu.user.domain.enums.OauthType;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;


@Slf4j
@Service
public class GoogleLoginService implements OauthService {

    @Value("${oauth.google.android-client-id}")
    private String googleAndroidClientId;

    @Value("${oauth.google.ios-client-id}")
    private String googleIosClientId;

    @Value("${oauth.google.client-id}")
    private String googleClientId;

    private GoogleIdTokenVerifier verifier;

    @PostConstruct
    public void init() {
        this.verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance())
                .setAudience(Arrays.asList(
                        googleClientId,
                        googleAndroidClientId,
                        googleIosClientId))
                .build();
    }

    @Override
    public OauthUserInfo verifyIdToken(String idToken) {
        GoogleIdToken token = verifyToken(idToken);
        GoogleIdToken.Payload payload = token.getPayload();
        return extractUserInfo(payload);
    }

    private GoogleIdToken verifyToken(String idToken) throws InvalidTokenException {
        try {
            GoogleIdToken token = verifier.verify(idToken);
            if (token == null) {
                log.warn("구글 ID Token 검증 실패 - verifier가 null 반환");
                throw new InvalidTokenException();
            }
            return token;
        } catch (GeneralSecurityException | IOException e) {
            log.warn("구글 ID Token 검증 중 예외 발생 - message: {}", e.getMessage(), e);
            throw new InvalidTokenException();
        }
    }

    private OauthUserInfo extractUserInfo(GoogleIdToken.Payload payload) {
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
    }

    @Override
    public OauthType getLoginType() { return OauthType.GOOGLE; }
}
