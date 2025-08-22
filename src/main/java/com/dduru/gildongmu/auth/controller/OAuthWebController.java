package com.dduru.gildongmu.auth.controller;

import com.dduru.gildongmu.auth.dto.LoginResponse;
import com.dduru.gildongmu.auth.service.OauthAuthService;
import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth/login")
@RequiredArgsConstructor
public class OAuthWebController {

    private final OauthAuthService oauthAuthService;

    @GetMapping("/{provider}")
    public ResponseEntity<Map<String, String>> getAuthorizationUrl(@PathVariable String provider) {
        String authUrl = oauthAuthService.getAuthorizationUrl(provider);
        return ResponseEntity.ok(Map.of("authUrl", authUrl));
    }

    @PostMapping("/{provider}")
    public ResponseEntity<LoginResponse> loginWithAuthCode(
            @PathVariable String provider,
            @RequestBody Map<String, String> request) {
        String code = requireNonBlank(request, "code", "Authorization Code");
        code = safeUrlDecode(code);
        LoginResponse response = oauthAuthService.processLogin(provider, code);
        return ResponseEntity.ok(response);
    }


    private String requireNonBlank(Map<String, String> request, String key, String desc) {
        if (request == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, desc + " 요청이 비어있습니다.");
        }
        String value = request.get(key);
        if (value == null || value.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, desc + "가 비어있습니다.");
        }
        return value;
    }

    private String safeUrlDecode(String code) {
        try {
            return URLDecoder.decode(code, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return code;
        }
    }
}
