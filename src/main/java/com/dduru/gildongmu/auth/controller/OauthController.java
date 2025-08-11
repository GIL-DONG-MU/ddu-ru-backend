package com.dduru.gildongmu.auth.controller;

import com.dduru.gildongmu.auth.dto.LoginResponse;
import com.dduru.gildongmu.auth.service.OauthAuthService;
import com.dduru.gildongmu.common.exception.BusinessException;
import com.dduru.gildongmu.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;

@Slf4j
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
        log.info("[{}] 로그인 성공 (authCode)", normalized);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{provider}")
    public ResponseEntity<LoginResponse> loginWithIdToken(
            @PathVariable String provider,
            @RequestParam(required = false) String idToken,
            @RequestBody(required = false) Map<String, String> request) {
        String normalized = validateAndNormalizeProvider(provider);
        String token = idToken != null ? idToken : requireNonBlank(request, "idToken", "ID Token");
        LoginResponse response = oauthAuthService.processTokenLogin(normalized, token);
        log.info("[{}] 로그인 성공 (idToken)", normalized);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refreshAccessToken(@RequestBody Map<String, String> request) {
        String refreshToken = requireNonBlank(request, "refreshToken", "Refresh Token");
        String userId = requireNonBlank(request, "userId", "User ID");

        log.info("Access Token 재발급 요청 - userId: {}", userId);

        LoginResponse response = oauthAuthService.refreshAccessToken(refreshToken, userId);

        log.info("Access Token 재발급 성공 - userId: {}", userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@RequestBody Map<String, String> request) {
        String userId = requireNonBlank(request, "userId", "User ID");

        log.info("로그아웃 요청 - userId: {}", userId);

        oauthAuthService.logout(userId);

        log.info("로그아웃 완료 - userId: {}", userId);
        return ResponseEntity.ok(Map.of("message", "로그아웃되었습니다."));
    }

    private String validateAndNormalizeProvider(String provider) {
        if (provider == null || provider.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "Provider가 비어있습니다.");
        }
        String normalized = provider.trim().toLowerCase();
        if (!SUPPORTED_PROVIDERS.contains(normalized)) {
            log.warn("지원하지 않는 OAuth Provider: {}", provider);
            throw new BusinessException(ErrorCode.UNSUPPORTED_SOCIAL_LOGIN, "지원하지 않는 소셜 로그인: " + provider);
        }
        return normalized;
    }

    private String requireNonBlank(Map<String, String> request, String key, String desc) {
        if (request == null) {
            log.error("요청 본문이 null임: {}", desc);
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, desc + " 요청이 비어있습니다.");
        }
        String value = request.get(key);
        if (value == null || value.isBlank()) {
            log.error("필수 파라미터 누락: {} ({})", key, desc);
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE, desc + "가 비어있습니다.");
        }
        return value;
    }

    private String safeUrlDecode(String code) {
        try {
            String decoded = URLDecoder.decode(code, StandardCharsets.UTF_8);
            if (!decoded.equals(code)) {
                log.debug("URL 디코딩 수행됨: 원본({}) -> 디코딩({})", code, decoded);
            }
            return decoded;
        } catch (Exception e) {
            log.warn("인증 코드 URL 디코딩 실패: {}", e.getMessage());
            return code;
        }
    }
}
