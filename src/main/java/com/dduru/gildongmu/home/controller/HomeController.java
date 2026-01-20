package com.dduru.gildongmu.home.controller;

import com.dduru.gildongmu.common.dto.ApiResult;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class HomeController implements HomeApiDocs {

    private static final String SERVICE_NAME = "뚜르";
    private static final String SERVICE_DESCRIPTION = "뚜르 - 여행 동행자 매칭 서비스";
    private static final String VERSION = "1.0.0";
    private static final String WELCOME_MESSAGE = "🎒 뚜르 API 서버에 오신 것을 환영합니다!";

    private final Environment environment;

    @Override
    @GetMapping("/")
    public ResponseEntity<ApiResult<Map<String, Object>>> welcome() {
        Map<String, Object> response = new LinkedHashMap<>();
        
        // 기본 정보
        response.put("service", SERVICE_NAME);
        response.put("description", SERVICE_DESCRIPTION);
        response.put("version", VERSION);
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());
        response.put("environment", getActiveProfile());
        
        // 인증 관련 엔드포인트
        Map<String, String> authEndpoints = new LinkedHashMap<>();
        authEndpoints.put("login_page", "/login/page");
        authEndpoints.put("kakao_login", "/api/v1/auth/kakao");
        authEndpoints.put("google_login", "/api/v1/auth/google");
        authEndpoints.put("refresh_token", "/api/v1/auth/refresh");
        authEndpoints.put("logout", "/api/v1/auth/logout");
        response.put("auth", authEndpoints);
        
        // 시스템 엔드포인트
        Map<String, String> systemEndpoints = new LinkedHashMap<>();
        systemEndpoints.put("health", "/actuator/health");
        systemEndpoints.put("swagger_ui", "/swagger-ui.html");
        systemEndpoints.put("api_docs", "/v3/api-docs");
        response.put("system", systemEndpoints);
        
        // OAuth 제공자 정보
        Map<String, String> oauthProviders = new LinkedHashMap<>();
        oauthProviders.put("kakao", "카카오 로그인 지원");
        oauthProviders.put("google", "구글 로그인 지원");
        response.put("oauth_providers", oauthProviders);
        
        response.put("message", WELCOME_MESSAGE);
        
        return ResponseEntity.ok(ApiResult.ok(response));
    }
    
    private String getActiveProfile() {
        String[] activeProfiles = environment.getActiveProfiles();
        if (activeProfiles.length == 0) {
            return "default";
        }
        return String.join(", ", activeProfiles);
    }
}
