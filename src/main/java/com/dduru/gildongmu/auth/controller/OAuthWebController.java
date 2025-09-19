package com.dduru.gildongmu.auth.controller;

import com.dduru.gildongmu.auth.dto.LoginResponse;
import com.dduru.gildongmu.auth.dto.web.WebLoginRequest;
import com.dduru.gildongmu.auth.dto.web.WebOAuthUrlResponse;
import com.dduru.gildongmu.auth.service.OauthAuthService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth/login")
@RequiredArgsConstructor
@Hidden
public class OAuthWebController {

    private final OauthAuthService oauthAuthService;

    @GetMapping("/{provider}")
    public ResponseEntity<WebOAuthUrlResponse> getAuthorizationUrl(
            @PathVariable String provider
    ) {
        WebOAuthUrlResponse response = oauthAuthService.getAuthorizationUrl(provider);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{provider}")
    public ResponseEntity<LoginResponse> loginWithAuthCode(
            @PathVariable String provider,
            @RequestBody WebLoginRequest request) {
        LoginResponse response = oauthAuthService.processLogin(provider, request);
        return ResponseEntity.ok(response);
    }
}
