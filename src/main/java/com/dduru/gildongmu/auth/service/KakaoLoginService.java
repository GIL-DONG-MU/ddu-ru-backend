package com.dduru.gildongmu.auth.service;

import com.dduru.gildongmu.auth.dto.OauthUserInfo;
import com.dduru.gildongmu.auth.dto.kakao.KakaoAccount;
import com.dduru.gildongmu.auth.dto.kakao.KakaoTokenResponse;
import com.dduru.gildongmu.auth.dto.kakao.KakaoUserResponse;
import com.dduru.gildongmu.auth.enums.OauthType;
import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoLoginService implements OauthService {

    private final WebClient webClient;

    @Value("${oauth.kakao.client-id}")
    private String kakaoClientId;

    @Value("${oauth.kakao.client-secret}")
    private String kakaoClientSecret;

    @Value("${oauth.kakao.redirect-uri}")
    private String kakaoRedirectUri;

    private static final String KAKAO_AUTH_URL = "https://kauth.kakao.com/oauth/authorize";
    private static final String KAKAO_TOKEN_URL = "https://kauth.kakao.com/oauth/token";
    private static final String KAKAO_USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";

    @Override
    public String getAuthorizationUrl() {
        // scope 파라미터 수정 (공백 제거)
        return KAKAO_AUTH_URL + "?" +
                "client_id=" + kakaoClientId +
                "&redirect_uri=" + kakaoRedirectUri +
                "&response_type=code" +
                "&scope=profile_nickname,profile_image,account_email,gender,age_range,phone_number";
    }

    @Override
    public String getAccessToken(String code) {
        try {
            KakaoTokenResponse response = webClient.post()
                    .uri(KAKAO_TOKEN_URL)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .bodyValue("grant_type=authorization_code" +
                            "&client_id=" + kakaoClientId +
                            "&client_secret=" + kakaoClientSecret +
                            "&code=" + code +
                            "&redirect_uri=" + kakaoRedirectUri)
                    .retrieve()
                    .bodyToMono(KakaoTokenResponse.class)
                    .block();

            if (response == null || response.accessToken() == null) {
                throw new BusinessException(ErrorCode.SOCIAL_LOGIN_FAILED, "카카오 액세스 토큰 응답이 null입니다.");
            }

            return response.accessToken();
        } catch (Exception e) {
            log.error("카카오 액세스 토큰 획득 실패", e);
            throw new BusinessException(ErrorCode.SOCIAL_LOGIN_FAILED, "카카오 액세스 토큰 획득 중 예외 발생");
        }
    }

    @Override
    public OauthUserInfo getUserInfo(String accessToken) {
        try {
            KakaoUserResponse response = webClient.get()
                    .uri(KAKAO_USER_INFO_URL)
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .bodyToMono(KakaoUserResponse.class)
                    .block();

            if (response == null) {
                throw new BusinessException(ErrorCode.SOCIAL_LOGIN_FAILED, "카카오 사용자 정보가 null입니다.");
            }

            KakaoAccount account = response.kakaoAccount();

            String gender = null;
            if (account.gender() != null &&
                    (account.genderNeedsAgreement() == null || !account.genderNeedsAgreement())) {
                gender = account.gender();
            }

            String ageRange = null;
            if (account.ageRange() != null &&
                    (account.ageRangeNeedsAgreement() == null || !account.ageRangeNeedsAgreement())) {
                ageRange = account.ageRange();
            }

            String phoneNumber = null;
            if (account.phoneNumber() != null &&
                    (account.phoneNumberNeedsAgreement() == null || !account.phoneNumberNeedsAgreement())) {
                phoneNumber = account.phoneNumber();
            }

            return OauthUserInfo.builder()
                    .oauthId(String.valueOf(response.id()))
                    .email(account.email())
                    .name(account.profile().nickname())
                    .profileImage(account.profile().profileImageUrl())
                    .loginType(OauthType.KAKAO)
                    .gender(gender)
                    .ageRange(ageRange)
                    .phoneNumber(phoneNumber)
                    .build();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("카카오 사용자 정보 조회 실패", e);
            throw new BusinessException(ErrorCode.SOCIAL_LOGIN_FAILED, "카카오 사용자 정보 조회 중 예외 발생");
        }
    }

    @Override
    public OauthType getLoginType() {
        return OauthType.KAKAO;
    }
}
