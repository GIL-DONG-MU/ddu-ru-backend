package com.dduru.gildongmu.auth.controller;

import com.dduru.gildongmu.auth.dto.LoginResponse;
import com.dduru.gildongmu.auth.service.OauthAuthService;
import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class OauthController {

    private final OauthAuthService oauthAuthService;

    @GetMapping("/login/{provider}")
    public ResponseEntity<Map<String, String>> getAuthorizationUrl(@PathVariable String provider) {
        if (provider == null || provider.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "Provider가 비어있습니다.");
        }

        if (!"kakao".equals(provider) && !"google".equals(provider)) {
            throw new BusinessException(ErrorCode.UNSUPPORTED_SOCIAL_LOGIN);
        }

        String authUrl = oauthAuthService.getAuthorizationUrl(provider);

        Map<String, String> response = new HashMap<>();
        response.put("authUrl", authUrl);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login/{provider}")
    public ResponseEntity<LoginResponse> login(@PathVariable String provider,
                                               @RequestBody(required = false) Map<String, String> request) {

        if (request == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "요청 본문이 비어있습니다.");
        }

        if (provider == null || provider.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "Provider가 비어있습니다.");
        }

        if (!"kakao".equals(provider) && !"google".equals(provider)) {
            throw new BusinessException(ErrorCode.UNSUPPORTED_SOCIAL_LOGIN);
        }

        String code = request.get("code");
        if (code == null || code.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "OAuth 인증 코드가 비어 있습니다.");
        }

        LoginResponse response = oauthAuthService.processLogin(provider, code);

        return ResponseEntity.ok(response);
    }
}
