package com.dduru.gildongmu.user.controller;

import com.dduru.gildongmu.common.annotation.CurrentUser;
import com.dduru.gildongmu.user.dto.UserUpdateNicknameRequest;
import com.dduru.gildongmu.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@RestController
public class UserController implements UserApiDocs {

    private final UserService userService;

    @Override
    @PutMapping("/nickname")
    public ResponseEntity<Void> updateNickname(@CurrentUser Long id, @Valid @RequestBody UserUpdateNicknameRequest request){
        userService.updateNickname(id, request);
        return ResponseEntity.noContent().build();
    }
}
