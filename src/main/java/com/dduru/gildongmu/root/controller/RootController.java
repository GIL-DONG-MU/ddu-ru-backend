package com.dduru.gildongmu.root.controller;

import com.dduru.gildongmu.common.dto.ApiResult;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Hidden
@RestController
@RequiredArgsConstructor
public class RootController {

    private static final String SERVICE_NAME = "뚜르";
    private static final String WELCOME_MESSAGE = "🎒 뚜르 API 서버에 오신 것을 환영합니다!";

    @GetMapping("/")
    public ResponseEntity<ApiResult<Map<String, Object>>> welcome() {

        Map<String, Object> response = Map.of(
                "service", SERVICE_NAME,
                "status", "UP",
                "message", WELCOME_MESSAGE
        );

        return ResponseEntity.ok(ApiResult.ok(response));
    }
}
