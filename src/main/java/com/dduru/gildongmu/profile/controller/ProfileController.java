package com.dduru.gildongmu.profile.controller;

import com.dduru.gildongmu.common.annotation.CurrentUser;
import com.dduru.gildongmu.common.dto.ApiResult;
import com.dduru.gildongmu.profile.dto.response.NicknameRandomResponse;
import com.dduru.gildongmu.profile.dto.response.NicknameValidateResponse;
import com.dduru.gildongmu.profile.dto.request.NicknameUpdateRequest;
import com.dduru.gildongmu.profile.dto.request.ProfileSetupRequest;
import com.dduru.gildongmu.profile.dto.request.ProfileUpdateRequest;
import com.dduru.gildongmu.profile.service.NicknameService;
import com.dduru.gildongmu.profile.service.ProfileManagementService;
import com.dduru.gildongmu.profile.service.ProfileSetupService;
import com.dduru.gildongmu.profile.validator.ValidNickname;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@RestController
public class ProfileController implements ProfileApiDocs {

    private final NicknameService nicknameService;
    private final ProfileSetupService profileSetupService;
    private final ProfileManagementService profileManagementService;

    @Override
    @PutMapping("/users/nickname")
    public ResponseEntity<ApiResult<Void>> updateNickname(
            @CurrentUser Long id,
            @Valid @RequestBody NicknameUpdateRequest request
    ) {
        nicknameService.updateNickname(id, request);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResult.noContent());
    }

    @Override
    @GetMapping("/nicknames/{nickname}/availability")
    public ResponseEntity<ApiResult<NicknameValidateResponse>> checkNickname(
            @PathVariable @ValidNickname String nickname
    ) {
        NicknameValidateResponse response = nicknameService.checkNickname(nickname);
        return ResponseEntity.ok(ApiResult.ok(response));
    }

    @Override
    @GetMapping("/nicknames/random")
    public ResponseEntity<ApiResult<NicknameRandomResponse>> generateRandomNickname() {
        NicknameRandomResponse response = nicknameService.generateRandomNickname();
        return ResponseEntity.ok(ApiResult.ok(response));
    }

    @Override
    @PatchMapping("/users/me/onboarding")
    public ResponseEntity<ApiResult<Void>> setupInitialProfile(
            @CurrentUser Long userId,
            @RequestBody @Valid ProfileSetupRequest request
    ) {
        profileSetupService.setupInitialProfile(userId, request);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResult.noContent());
    }

    @Override
    @PatchMapping("/users/me/profile")
    public ResponseEntity<ApiResult<Void>> updateProfile(
            @CurrentUser Long userId,
            @RequestBody @Valid ProfileUpdateRequest request
    ){
        profileManagementService.updateProfile(userId, request);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResult.noContent());
    }
}
