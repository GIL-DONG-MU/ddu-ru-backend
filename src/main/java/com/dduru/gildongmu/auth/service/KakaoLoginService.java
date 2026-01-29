package com.dduru.gildongmu.auth.service;

import com.dduru.gildongmu.auth.dto.OauthUserInfo;
import com.dduru.gildongmu.auth.exception.InvalidTokenException;
import com.dduru.gildongmu.user.enums.OauthType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.Optional;

@Slf4j
@Service
public class KakaoLoginService implements OauthService {

    @Value("${oauth.kakao.client-id}")
    private String kakaoClientId;

    @Value("${oauth.kakao.rest-client-id}")
    private String kakaoRestClientId;

    private final ObjectMapper objectMapper;
    private final KakaoUserInfoMapper userInfoMapper;

    public KakaoLoginService() {
        this.objectMapper = new ObjectMapper();
        this.userInfoMapper = new KakaoUserInfoMapper();
    }

    @Override
    public OauthUserInfo verifyIdToken(String idToken) {
        log.debug("카카오 ID Token 검증 시작");
        
        JsonNode payload = parseIdTokenPayload(idToken);
        validateAudience(payload);
        OauthUserInfo userInfo = userInfoMapper.mapFromIdToken(payload);
        
        log.debug("카카오 ID Token 검증 완료 - oauthId: {}", userInfo.oauthId());
        return userInfo;
    }

    private JsonNode parseIdTokenPayload(String idToken) throws InvalidTokenException {
        String[] chunks = idToken.split("\\.");
        if (chunks.length != 3) {
            throw new InvalidTokenException("잘못된 카카오 ID Token 형식입니다. (JWT 형식이 아닙니다)");
        }

        try {
            Base64.Decoder decoder = Base64.getUrlDecoder();
            String payload = new String(decoder.decode(chunks[1]));
            return objectMapper.readTree(payload);
        } catch (IllegalArgumentException e) {
            throw new InvalidTokenException("카카오 ID Token의 payload를 디코딩할 수 없습니다.");
        } catch (Exception e) {
            throw new InvalidTokenException("카카오 ID Token의 payload를 파싱할 수 없습니다.");
        }
    }

    private void validateAudience(JsonNode payload) {
        String aud = payload.get("aud").asText();
        if (!kakaoClientId.equals(aud) && !kakaoRestClientId.equals(aud)) {
            throw new InvalidTokenException("잘못된 카카오 클라이언트 ID입니다.");
        }
    }

    private static class KakaoUserInfoMapper {

        public OauthUserInfo mapFromIdToken(JsonNode payload) {
            return OauthUserInfo.builder()
                    .oauthId(payload.get("sub").asText())
                    .email(getJsonValue(payload, "email"))
                    .name(getJsonValue(payload, "nickname"))
                    .profileImage(getJsonValue(payload, "picture"))
                    .loginType(OauthType.KAKAO)
                    // 회원가입 시 기본 정보만 받으므로 추가 정보는 추출하지 않음
                    // .gender(getJsonValue(payload, "gender"))
                    // .phoneNumber(getJsonValue(payload, "phone_number"))
                    .build();
        }

        private String getJsonValue(JsonNode json, String key) {
            return Optional.ofNullable(json.get(key))
                    .filter(node -> !node.isNull())
                    .map(JsonNode::asText)
                    .orElse(null);
        }
    }

    @Override
    public OauthType getLoginType() { return OauthType.KAKAO; }
}
