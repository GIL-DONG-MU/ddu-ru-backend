package com.dduru.gildongmu.fcm.controller;

import com.dduru.gildongmu.common.annotation.CurrentUser;
import com.dduru.gildongmu.common.dto.ApiResult;
import com.dduru.gildongmu.fcm.dto.request.FcmTokenDeleteRequest;
import com.dduru.gildongmu.fcm.dto.request.FcmTokenRegisterRequest;
import com.dduru.gildongmu.fcm.service.FcmTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/fcm/token")
public class FcmTokenController implements FcmTokenApiDocs {

    private final FcmTokenService fcmTokenService;

    @Override
    @PostMapping
    public ResponseEntity<ApiResult<Void>> register(
            @CurrentUser Long userId,
            @Valid @RequestBody FcmTokenRegisterRequest request
    ) {
        fcmTokenService.register(userId, request);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResult.noContent());
    }

    @Override
    @DeleteMapping
    public ResponseEntity<ApiResult<Void>> delete(
            @CurrentUser Long userId,
            @Valid @RequestBody FcmTokenDeleteRequest request
    ) {
        fcmTokenService.delete(userId, request);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResult.noContent());
    }
}
