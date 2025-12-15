package com.dduru.gildongmu.user.controller;

import com.dduru.gildongmu.common.annotation.CurrentUser;
import com.dduru.gildongmu.common.dto.ApiResult;
import com.dduru.gildongmu.user.dto.NicknameRandomResponse;
import com.dduru.gildongmu.user.dto.UserCheckNicknameResponse;
import com.dduru.gildongmu.user.dto.UserUpdateNicknameRequest;
import com.dduru.gildongmu.user.service.UserService;
import com.dduru.gildongmu.user.validator.ValidNickname;
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
public class UserController implements UserApiDocs {

    private final UserService userService;

    @Override
    @PutMapping("/users/nickname")
    public ResponseEntity<ApiResult<Void>> updateNickname(
            @CurrentUser Long id,
            @Valid @RequestBody UserUpdateNicknameRequest request
    ) {
        userService.updateNickname(id, request);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResult.noContent());
    }

    @Override
    @GetMapping("/nicknames/{nickname}/availability")
    public ResponseEntity<ApiResult<UserCheckNicknameResponse>> checkNickname(
            @PathVariable @ValidNickname String nickname
    ) {
        UserCheckNicknameResponse response = userService.checkNickname(nickname);
        return ResponseEntity.ok(ApiResult.ok(response));
    }

    @Override
    @GetMapping("/nicknames/random")
    public ResponseEntity<ApiResult<NicknameRandomResponse>> generateRandomNickname() {
        NicknameRandomResponse response = userService.generateRandomNickname();
        return ResponseEntity.ok(ApiResult.ok(response));
    }
}
