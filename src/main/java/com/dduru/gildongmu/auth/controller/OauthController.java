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
        validateProvider(provider);

        String authUrl = oauthAuthService.getAuthorizationUrl(provider);
        Map<String, String> response = new HashMap<>();
        response.put("authUrl", authUrl);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login/{provider}")
    public ResponseEntity<LoginResponse> login(@PathVariable String provider,
                                               @RequestBody(required = false) Map<String, String> request) {

        validateProvider(provider);
        validateRequest(request, "code", "OAuth 인증 코드");

        String code = request.get("code");
        LoginResponse response = oauthAuthService.processLogin(provider, code);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{provider}")
    public ResponseEntity<LoginResponse> loginWithToken(
            @PathVariable String provider,
            @RequestBody Map<String, String> request) {
        validateProvider(provider);
        validateRequest(request, "idToken", "ID Token");

        String idToken = request.get("idToken");
        LoginResponse response = oauthAuthService.processTokenLogin(provider, idToken);
        return ResponseEntity.ok(response);
    }

    private void validateProvider(String provider) {
        if (provider == null || provider.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "Provider가 비어있습니다.");
        }

        if (!"kakao".equals(provider) && !"google".equals(provider)) {
            throw new BusinessException(ErrorCode.UNSUPPORTED_SOCIAL_LOGIN);
        }
    }

    private void validateRequest(Map<String, String> request, String key, String description) {
        if (request == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "요청 본문이 비어있습니다.");
        }

        String value = request.get(key);
        if (value == null || value.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, description + "가 비어 있습니다.");
        }
    }
}
