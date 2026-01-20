package com.dduru.gildongmu.auth.controller;

import com.dduru.gildongmu.auth.utils.OAuthRedirectUriHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.StringJoiner;

@Controller
@RequiredArgsConstructor
public class OAuthPageController {

    private final OAuthRedirectUriHelper redirectUriHelper;

    @Value("${oauth.kakao.rest-client-id}")
    private String kClientId;

    @Value("${oauth.google.client-id}")
    private String gClientId;

    @GetMapping("/login/page")
    public String loginPage(Model model) {
        // Kakao URL
        String kakaoUrl = new StringJoiner("&")
                .add("https://kauth.kakao.com/oauth/authorize?response_type=code")
                .add("client_id=" + kClientId)
                .add("redirect_uri=" + redirectUriHelper.getKakaoRedirectUri())
                .add("scope=openid profile_nickname profile_image account_email")
                .toString();
        model.addAttribute("kakaoUrl", kakaoUrl);

        // Google URL
        String googleUrl = new StringJoiner("&")
                .add("https://accounts.google.com/o/oauth2/v2/auth?response_type=code")
                .add("client_id=" + gClientId)
                .add("redirect_uri=" + redirectUriHelper.getGoogleRedirectUri())
                .add("scope=openid profile email")
                .toString();
        model.addAttribute("googleUrl", googleUrl);

        return "login";
    }
}
