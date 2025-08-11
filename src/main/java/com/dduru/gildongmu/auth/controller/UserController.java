package com.dduru.gildongmu.auth.controller;

import com.dduru.gildongmu.auth.dto.NicknameUpdateRequest;
import com.dduru.gildongmu.auth.dto.NicknameUpdateResponse;
import com.dduru.gildongmu.auth.service.UserService;
import com.dduru.gildongmu.common.annotation.CurrentUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @PatchMapping("/nickname")
    public ResponseEntity<NicknameUpdateResponse> updateNickname(
            @CurrentUser Long userId,
            @Valid @RequestBody NicknameUpdateRequest request) {
        userService.updateNickname(userId, request.nickname());
        NicknameUpdateResponse response = NicknameUpdateResponse.of(request.nickname());
        return ResponseEntity.ok(response);
    }
}