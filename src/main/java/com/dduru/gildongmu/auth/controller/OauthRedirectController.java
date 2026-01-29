package com.dduru.gildongmu.auth.controller;

import com.dduru.gildongmu.auth.dto.local.LocalGoogleTokenResponse;
import com.dduru.gildongmu.auth.dto.local.LocalKakaoTokenResponse;
import com.dduru.gildongmu.auth.utils.OAuthRedirectUriHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Controller
@RequiredArgsConstructor
public class OauthRedirectController {

    private final OAuthRedirectUriHelper redirectUriHelper;

    // Kakao 설정
    @Value("${oauth.kakao.rest-client-id}")
    private String kClientId;

    @Value("${oauth.kakao.client-secret}")
    private String kClientSecret;

    // Google 설정
    @Value("${oauth.google.client-id}")
    private String gClientId;

    @Value("${oauth.google.client-secret}")
    private String gClientSecret;
    

    private static final String KAKAO = "Kakao";
    private static final String GOOGLE = "Google";

    @GetMapping("/test/login/oauth2/code/kakao")
    public String kakaoCallback(@RequestParam("code") String code, Model model) {
        log.info("카카오 테스트용 인증 코드를 받았습니다.");

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "authorization_code");
        formData.add("client_id", kClientId);
        formData.add("redirect_uri", redirectUriHelper.getKakaoRedirectUri());
        formData.add("code", code);
        formData.add("client_secret", kClientSecret);

        try {
            LocalKakaoTokenResponse tokenResponse = WebClient.create("https://kauth.kakao.com").post()
                    .uri("/oauth/token")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue(formData)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> clientResponse.bodyToMono(String.class).flatMap(errorBody -> {
                                log.error("{} 서버 에러: {}", KAKAO, errorBody);
                                return Mono.error(new RuntimeException(errorBody));
                            }))
                    .bodyToMono(LocalKakaoTokenResponse.class)
                    .block();

            String idToken = (tokenResponse != null) ? tokenResponse.idToken() : KAKAO + " idToken 조회 실패.";
            log.info("{} idToken: {}", KAKAO, idToken);

            model.addAttribute("provider", KAKAO);
            model.addAttribute("idToken", idToken);

            return "login-success";

        } catch (Exception e) {
            model.addAttribute("provider", KAKAO);
            model.addAttribute("error", e.getMessage());
            return "login-error";
        }
    }

    @GetMapping("/test/login/oauth2/code/google")
    public String googleCallback(@RequestParam("code") String code, Model model) {
        log.info("구글 테스트용 인증 코드를 받았습니다.");

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "authorization_code");
        formData.add("client_id", gClientId);
        formData.add("redirect_uri", redirectUriHelper.getGoogleRedirectUri());
        formData.add("code", code);
        formData.add("client_secret", gClientSecret);

        try {
            LocalGoogleTokenResponse tokenResponse = WebClient.create("https://oauth2.googleapis.com").post()
                    .uri("/token")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue(formData)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            clientResponse -> clientResponse.bodyToMono(String.class).flatMap(errorBody -> {
                                log.error("{} 서버 에러: {}", GOOGLE, errorBody);
                                return Mono.error(new RuntimeException(errorBody));
                            }))
                    .bodyToMono(LocalGoogleTokenResponse.class)
                    .block();

            String idToken = (tokenResponse != null) ? tokenResponse.idToken() : GOOGLE + " idToken 조회 실패.";
            log.info("{} idToken: {}", GOOGLE, idToken);

            model.addAttribute("provider", GOOGLE);
            model.addAttribute("idToken", idToken);

            return "login-success";

        } catch (Exception e) {
            model.addAttribute("provider", GOOGLE);
            model.addAttribute("error", e.getMessage());
            return "login-error";
        }
    }
}
