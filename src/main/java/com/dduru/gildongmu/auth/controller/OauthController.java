package com.dduru.gildongmu.auth.controller;

import com.dduru.gildongmu.auth.dto.request.LoginRequest;
import com.dduru.gildongmu.auth.dto.response.LoginResponse;
import com.dduru.gildongmu.auth.dto.request.RefreshTokenRequest;
import com.dduru.gildongmu.auth.service.OauthAuthService;
import com.dduru.gildongmu.common.annotation.CurrentUser;
import com.dduru.gildongmu.common.dto.ApiResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class OauthController implements OauthApiDocs {

    private final OauthAuthService oauthAuthService;

    @Override
    @PostMapping("/{provider}")
    public ResponseEntity<ApiResult<LoginResponse>> loginWithIdToken(
            @PathVariable String provider,
            @Valid @RequestBody LoginRequest request
    ) {
        LoginResponse response = oauthAuthService.processTokenLogin(provider, request);
        return ResponseEntity.ok(ApiResult.ok(response));
    }

    @Override
    @PostMapping("/refresh")
    public ResponseEntity<ApiResult<LoginResponse>> refreshAccessToken(@Valid @RequestBody RefreshTokenRequest request) {
        LoginResponse response = oauthAuthService.refreshAccessToken(request.refreshToken());
        return ResponseEntity.ok(ApiResult.ok(response));
    }

    @Override
    @PostMapping("/logout")
    public ResponseEntity<ApiResult<Void>> logout(@CurrentUser Long userId) {
        oauthAuthService.logout(userId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResult.noContent());
    }
}
