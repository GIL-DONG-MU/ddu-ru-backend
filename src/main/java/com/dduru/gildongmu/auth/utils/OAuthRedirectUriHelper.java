package com.dduru.gildongmu.auth.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OAuthRedirectUriHelper {

    private static final String PROD_KAKAO_REDIRECT_URI = "https://api.dduru.app/test/login/oauth2/code/kakao";
    private static final String PROD_GOOGLE_REDIRECT_URI = "https://api.dduru.app/test/login/oauth2/code/google";
    private static final String DEV_KAKAO_REDIRECT_URI = "http://localhost:8080/test/login/oauth2/code/kakao";
    private static final String DEV_GOOGLE_REDIRECT_URI = "http://localhost:8080/test/login/oauth2/code/google";

    private final Environment environment;

    public String getKakaoRedirectUri() {
        return isProdProfile() ? PROD_KAKAO_REDIRECT_URI : DEV_KAKAO_REDIRECT_URI;
    }

    public String getGoogleRedirectUri() {
        return isProdProfile() ? PROD_GOOGLE_REDIRECT_URI : DEV_GOOGLE_REDIRECT_URI;
    }

    private boolean isProdProfile() {
        String[] activeProfiles = environment.getActiveProfiles();
        for (String profile : activeProfiles) {
            if ("prod".equals(profile)) {
                return true;
            }
        }
        return false;
    }
}
