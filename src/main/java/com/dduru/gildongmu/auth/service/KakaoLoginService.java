package com.dduru.gildongmu.auth.service;

import com.dduru.gildongmu.auth.dto.OauthUserInfo;
import com.dduru.gildongmu.auth.exception.InvalidTokenException;
import com.dduru.gildongmu.user.enums.OauthType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Base64;
import java.util.Optional;

@Slf4j
@Service
public class KakaoLoginService extends AbstractOauthService {

    @Value("${oauth.kakao.client-id}")
    private String kakaoClientId;

    @Value("${oauth.kakao.rest-client-id}")
    private String kakaoRestClientId;

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

        } catch (IllegalArgumentException e) {
            log.warn("카카오 ID Token 파싱 실패: {}", e.getMessage(), e);
            throw new InvalidTokenException("잘못된 카카오 ID Token 형식입니다.");
        } catch (Exception e) {
            log.error("카카오 ID Token 검증 중 예상치 못한 오류 발생", e);
            handleOauthException(e, "카카오 ID Token 검증");
            throw new AssertionError("handleOauthException이 예외를 throw해야 하는데 throw하지 않았습니다.");
        }
    }

    private JsonNode parseIdTokenPayload(String idToken) throws Exception {
        String[] chunks = idToken.split("\\.");
        if (chunks.length != 3) {
            log.warn("카카오 ID Token 형식 오류: JWT 형식이 아님 (chunks.length: {})", chunks.length);
            throw new InvalidTokenException("잘못된 카카오 ID Token 형식입니다. (JWT 형식이 아닙니다)");
        }

        try {
            Base64.Decoder decoder = Base64.getUrlDecoder();
            String payload = new String(decoder.decode(chunks[1]));
            return objectMapper.readTree(payload);
        } catch (IllegalArgumentException e) {
            log.warn("카카오 ID Token payload 디코딩 실패: {}", e.getMessage(), e);
            throw new InvalidTokenException("카카오 ID Token의 payload를 디코딩할 수 없습니다.");
        } catch (Exception e) {
            log.error("카카오 ID Token payload 파싱 실패", e);
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
    protected String getClientId() { return kakaoClientId; }
    @Override
    public OauthType getLoginType() { return OauthType.KAKAO; }
}
