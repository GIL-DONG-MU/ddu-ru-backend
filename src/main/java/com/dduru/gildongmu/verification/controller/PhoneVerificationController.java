package com.dduru.gildongmu.verification.controller;

import com.dduru.gildongmu.common.annotation.CurrentUser;
import com.dduru.gildongmu.common.dto.ApiResult;
import com.dduru.gildongmu.verification.dto.VerificationSendRequest;
import com.dduru.gildongmu.verification.dto.VerificationSendResponse;
import com.dduru.gildongmu.verification.dto.VerificationVerifyRequest;
import com.dduru.gildongmu.verification.dto.VerificationVerifyResponse;
import com.dduru.gildongmu.verification.service.PhoneVerificationService;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1/verifications")
@RequiredArgsConstructor
@RestController
public class PhoneVerificationController implements PhoneVerificationApiDocs {

    private final PhoneVerificationService phoneVerificationService;

    @Override
    @PostMapping
    public ResponseEntity<ApiResult<VerificationSendResponse>> sendVerificationCode(
            @CurrentUser Long userId,
            @Valid @RequestBody VerificationSendRequest request) {
        VerificationSendResponse response = phoneVerificationService.sendVerificationCode(request.phoneNumber());
        return ResponseEntity.ok(ApiResult.ok(response));
    }

    @Override
    @PatchMapping
    public ResponseEntity<ApiResult<VerificationVerifyResponse>> verifyCode(
            @CurrentUser Long userId,
            @Valid @RequestBody VerificationVerifyRequest request) {
        VerificationVerifyResponse response = phoneVerificationService.verifyCode(
                request.phoneNumber(),
                request.code()
        );
        return ResponseEntity.ok(ApiResult.ok(response));
    }

    @Override
    @PostMapping("/admin")
    public ResponseEntity<ApiResult<VerificationSendResponse>> sendVerificationCodeAdmin(
            @Parameter(hidden = true) @CurrentUser Long userId,
            @Valid @RequestBody VerificationSendRequest request) {
        VerificationSendResponse response = phoneVerificationService.sendVerificationCodeAdmin(request.phoneNumber());
        return ResponseEntity.ok(ApiResult.ok(response));
    }
}
