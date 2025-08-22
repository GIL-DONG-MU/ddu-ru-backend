package com.dduru.gildongmu.auth.controller;

import com.dduru.gildongmu.auth.dto.LoginRequest;
import com.dduru.gildongmu.auth.dto.LoginResponse;
import com.dduru.gildongmu.auth.dto.RefreshTokenRequest;
import com.dduru.gildongmu.auth.service.OauthAuthService;
import com.dduru.gildongmu.common.annotation.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class OauthController implements OauthApiDocs {

    private final OauthAuthService oauthAuthService;

    @Override
    @PostMapping("/{provider}")
    public ResponseEntity<LoginResponse> loginWithIdToken(
            @PathVariable String provider,
            @RequestBody(required = false) LoginRequest request
    ) {
        LoginResponse response = oauthAuthService.processTokenLogin(provider, request);
        return ResponseEntity.ok(response);
    }

    @Override
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refreshAccessToken(@Valid @RequestBody RefreshTokenRequest request) {
        LoginResponse response = oauthAuthService.refreshAccessToken(request.refreshToken());
        return ResponseEntity.ok(response);
    }

    @Override
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@CurrentUser Long userId) {
        oauthAuthService.logout(String.valueOf(userId));
        return ResponseEntity.noContent().build();
    }
}
