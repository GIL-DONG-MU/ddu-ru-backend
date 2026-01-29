package com.dduru.gildongmu.auth.service;

import com.dduru.gildongmu.auth.dto.OauthUserInfo;
import com.dduru.gildongmu.auth.exception.InvalidTokenException;
import com.dduru.gildongmu.user.enums.OauthType;
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
public class GoogleLoginService extends AbstractOauthService {

    @Value("${oauth.google.android-client-id}")
    private String googleAndroidClientId;

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
                        googleAndroidClientId))
                .build();
    }

    @Override
    public OauthUserInfo verifyIdToken(String idToken) {
        log.debug("구글 ID Token 검증 시작");
        
        GoogleIdToken token = verifyToken(idToken);
        GoogleIdToken.Payload payload = token.getPayload();
        OauthUserInfo userInfo = extractUserInfo(payload);
        
        log.debug("구글 ID Token 검증 완료 - oauthId: {}", userInfo.oauthId());
        return userInfo;
    }

    private GoogleIdToken verifyToken(String idToken) throws InvalidTokenException {
        try {
            GoogleIdToken token = verifier.verify(idToken);
            if (token == null) {
                throw new InvalidTokenException("유효하지 않은 구글 ID Token입니다.");
            }
            return token;
        } catch (GeneralSecurityException | IOException e) {
            throw new InvalidTokenException("구글 ID Token 검증 중 오류가 발생했습니다.");
        }
    }

    private OauthUserInfo extractUserInfo(GoogleIdToken.Payload payload) {
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
    }

    @Override
    protected String getClientId() { return googleClientId; }
    @Override
    public OauthType getLoginType() { return OauthType.GOOGLE; }
}
