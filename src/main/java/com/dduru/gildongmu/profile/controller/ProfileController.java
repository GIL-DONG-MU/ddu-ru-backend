package com.dduru.gildongmu.profile.controller;

import com.dduru.gildongmu.common.annotation.CurrentUser;
import com.dduru.gildongmu.common.dto.ApiResult;
import com.dduru.gildongmu.profile.dto.NicknameRandomResponse;
import com.dduru.gildongmu.profile.dto.NicknameValidateResponse;
import com.dduru.gildongmu.profile.dto.NicknameUpdateRequest;
import com.dduru.gildongmu.profile.dto.ProfileSetupRequest;
import com.dduru.gildongmu.profile.service.ProfileService;
import com.dduru.gildongmu.profile.validator.ValidNickname;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
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

    private final ProfileService profileService;

    @Override
    @PutMapping("/users/nickname")
    public ResponseEntity<ApiResult<Void>> updateNickname(
            @CurrentUser Long id,
            @Valid @RequestBody NicknameUpdateRequest request
    ) {
        profileService.updateNickname(id, request);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResult.noContent());
    }

    @Override
    @GetMapping("/nicknames/{nickname}/availability")
    public ResponseEntity<ApiResult<NicknameValidateResponse>> checkNickname(
            @PathVariable @ValidNickname String nickname
    ) {
        NicknameValidateResponse response = profileService.checkNickname(nickname);
        return ResponseEntity.ok(ApiResult.ok(response));
    }

    @Override
    @GetMapping("/nicknames/random")
    public ResponseEntity<ApiResult<NicknameRandomResponse>> generateRandomNickname() {
        NicknameRandomResponse response = profileService.generateRandomNickname();
        return ResponseEntity.ok(ApiResult.ok(response));
    }

    @Override
    @PutMapping("/me/profile")
    public ResponseEntity<ApiResult<Void>> setupInitialProfile(
            @CurrentUser Long userId,
            @RequestBody @Valid ProfileSetupRequest request
    ) {
        profileService.setupInitialProfile(userId, request);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResult.noContent());
    }
}
