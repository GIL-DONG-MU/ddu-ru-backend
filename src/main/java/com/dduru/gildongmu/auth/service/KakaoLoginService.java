package com.dduru.gildongmu.auth.service;

import com.dduru.gildongmu.auth.dto.OauthUserInfo;
import com.dduru.gildongmu.auth.exception.InvalidTokenException;
import com.dduru.gildongmu.user.enums.OauthType;
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

    private final ObjectMapper objectMapper;
    private final KakaoUserInfoMapper userInfoMapper;

    public KakaoLoginService(WebClient.Builder webClientBuilder) {
        super(webClientBuilder);
        this.objectMapper = new ObjectMapper();
        this.userInfoMapper = new KakaoUserInfoMapper();
    }

    @Override
    public OauthUserInfo verifyIdToken(String idToken) {
        try {
            JsonNode payload = parseIdTokenPayload(idToken);
            validateAudience(payload);

            return userInfoMapper.mapFromIdToken(payload);

        } catch (InvalidTokenException e) {
            throw e;
        } catch (Exception e) {
            handleOauthException(e, "카카오 ID Token 검증");
            throw new AssertionError("handleOauthException은 항상 예외를 던집니다.");
        }
    }

    private JsonNode parseIdTokenPayload(String idToken) throws Exception {
        String[] chunks = idToken.split("\\.");
        if (chunks.length != 3) {
            throw new InvalidTokenException("잘못된 카카오 ID Token 형식입니다.");
        }

        Base64.Decoder decoder = Base64.getUrlDecoder();
        String payload = new String(decoder.decode(chunks[1]));
        return objectMapper.readTree(payload);
    }

    private void validateAudience(JsonNode payload) {
        if (!getClientId().equals(payload.get("aud").asText())) {
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
    protected String getClientId() { return kakaoClientId; }
    @Override
    public OauthType getLoginType() { return OauthType.KAKAO; }
}
