package com.dduru.gildongmu.auth.controller;

import com.dduru.gildongmu.auth.dto.LoginResponse;
import com.dduru.gildongmu.auth.dto.RefreshTokenRequest;
import com.dduru.gildongmu.auth.service.OauthAuthService;
import com.dduru.gildongmu.common.annotation.CurrentUser;
import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class OauthController {

    private final OauthAuthService oauthAuthService;
    private static final Set<String> SUPPORTED_PROVIDERS = Set.of("kakao", "google");

    @GetMapping("/login/{provider}")
    public ResponseEntity<Map<String, String>> getAuthorizationUrl(@PathVariable String provider) {
        String normalized = validateAndNormalizeProvider(provider);
        String authUrl = oauthAuthService.getAuthorizationUrl(normalized);
        return ResponseEntity.ok(Map.of("authUrl", authUrl));
    }

    @PostMapping("/login/{provider}")
    public ResponseEntity<LoginResponse> loginWithAuthCode(
            @PathVariable String provider,
            @RequestBody Map<String, String> request) {
        String normalized = validateAndNormalizeProvider(provider);
        String code = requireNonBlank(request, "code", "Authorization Code");
        code = safeUrlDecode(code);
        LoginResponse response = oauthAuthService.processLogin(normalized, code);
        return ResponseEntity.ok(response);
    }

    /*
    아래부터 Mobile용 API
     */

    @PostMapping("/{provider}")
    public ResponseEntity<LoginResponse> loginWithIdToken(
            @PathVariable String provider,
            @RequestParam(required = false) String idToken,
            @RequestBody(required = false) Map<String, String> request
    ) {
        String normalized = validateAndNormalizeProvider(provider);
        String token = idToken != null ? idToken : requireNonBlank(request, "idToken", "ID Token");
        LoginResponse response = oauthAuthService.processTokenLogin(normalized, token);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refreshAccessToken(@Valid @RequestBody RefreshTokenRequest request) {
        LoginResponse response = oauthAuthService.refreshAccessToken(request.refreshToken());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@CurrentUser Long userId) {
        oauthAuthService.logout(String.valueOf(userId));
        return ResponseEntity.noContent().build();
    }

    private String validateAndNormalizeProvider(String provider) {
        if (provider == null || provider.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "Provider가 비어있습니다.");
        }
        String normalized = provider.trim().toLowerCase();
        if (!SUPPORTED_PROVIDERS.contains(normalized)) {
            throw new BusinessException(ErrorCode.UNSUPPORTED_SOCIAL_LOGIN, "지원하지 않는 소셜 로그인: " + provider);
        }
        return normalized;
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
