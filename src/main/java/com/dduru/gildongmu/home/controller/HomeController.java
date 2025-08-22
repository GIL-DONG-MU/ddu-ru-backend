package com.dduru.gildongmu.home.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
public class HomeController implements HomeApiDocs {

    @Value("${spring.application.name:길동무}")
    private String applicationName;

    @Override
    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> welcome() {
        Map<String, Object> response = new HashMap<>();
        
        response.put("service", applicationName);
        response.put("description", "길동무 - 여행 동행자 매칭 서비스");
        response.put("version", "1.0.0");
        response.put("status", "UP");
        response.put("timestamp", LocalDateTime.now());
        
        Map<String, String> endpoints = new HashMap<>();
        endpoints.put("health", "/actuator/health");
        endpoints.put("kakao_login", "/api/v1/auth/kakao");
        endpoints.put("google_login", "/api/v1/auth/google");
        
        response.put("endpoints", endpoints);
        response.put("message", "🎒 길동무 API 서버에 오신 것을 환영합니다!");
        
        return ResponseEntity.ok(response);
    }
}
