package com.dduru.gildongmu.auth.service;

import com.dduru.gildongmu.auth.dto.OauthUserInfo;
import com.dduru.gildongmu.auth.exception.InvalidTokenException;
import com.dduru.gildongmu.user.domain.enums.OauthType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoLoginService implements OauthService {

    @Value("${oauth.kakao.client-id}")
    private String kakaoClientId;

    @Value("${oauth.kakao.rest-client-id}")
    private String kakaoRestClientId;

    private final ObjectMapper objectMapper;

    @Override
    public OauthUserInfo verifyIdToken(String idToken) {
        JsonNode payload = parseIdTokenPayload(idToken);
        validateAudience(payload);
        return extractUserInfo(payload);
    }

    @Override
    public OauthType getLoginType() {
        return OauthType.KAKAO;
    }

    /**
     * 카카오 ID Token의 payload를 파싱
     * 보안: 현재는 Base64 디코딩만 수행
     * 앱에서 받은 토큰은 카카오 공개키(JWK)로 서명 검증이 필요.
     */
    private JsonNode parseIdTokenPayload(String idToken) throws InvalidTokenException {
        String[] chunks = idToken.split("\\.");
        if (chunks.length != 3) {
            log.warn("카카오 ID Token 형식 오류 - tokenParts: {}", chunks.length);
            throw new InvalidTokenException();
        }

        try {
            Base64.Decoder decoder = Base64.getUrlDecoder();
            String payload = new String(decoder.decode(chunks[1]));
            return objectMapper.readTree(payload);
        } catch (IllegalArgumentException e) {
            log.warn("카카오 ID Token 디코딩 실패 - payload segment 손상, message: {}", e.getMessage());
            throw new InvalidTokenException();
        } catch (Exception e) {
            log.warn("카카오 ID Token 파싱 실패 - message: {}", e.getMessage(), e);
            throw new InvalidTokenException();
        }
    }

    private void validateAudience(JsonNode payload) {
        JsonNode audNode = payload.path("aud");
        if (audNode.isMissingNode()) {
            log.warn("카카오 토큰에 aud 필드 없음");
            throw new InvalidTokenException();
        }
        String aud = audNode.asText();
        if (!kakaoClientId.equals(aud) && !kakaoRestClientId.equals(aud)) {
            log.warn("카카오 토큰 audience 불일치 - aud: {}", aud);
            throw new InvalidTokenException();
        }
    }

    private OauthUserInfo extractUserInfo(JsonNode payload) {
        JsonNode subNode = payload.path("sub");
        if (subNode.isMissingNode()) {
            log.warn("카카오 토큰에 sub 필드 없음");
            throw new InvalidTokenException();
        }
        return OauthUserInfo.builder()
                .oauthId(subNode.asText())
                .email(payload.path("email").asText(null))
                .name(payload.path("nickname").asText(null))
                .loginType(OauthType.KAKAO)
                // 회원가입 시 기본 정보만 받으므로 추가 정보는 추출하지 않음
                // .profileImage(payload.path("picture").asText(null))
                // .gender(payload.path("gender").asText(null))
                // .phoneNumber(payload.path("phone_number").asText(null))
                .build();
    }
}
