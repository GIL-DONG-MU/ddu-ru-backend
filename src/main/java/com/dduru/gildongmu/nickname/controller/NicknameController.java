package com.dduru.gildongmu.nickname.controller;

import com.dduru.gildongmu.common.dto.ApiResult;
import com.dduru.gildongmu.nickname.dto.NicknameGenerateResponse;
import com.dduru.gildongmu.nickname.service.NicknameGeneratorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 랜덤 닉네임 생성 API 컨트롤러
 */
@RequiredArgsConstructor
@RequestMapping("/api/v1/nicknames")
@RestController
public class NicknameController implements NicknameApiDocs {

    private final NicknameGeneratorService nicknameGeneratorService;

    /**
     * 랜덤 닉네임 생성
     * 중복되지 않는 유니크한 닉네임을 하나 생성합니다.
     */
    @Override
    @GetMapping("/random")
    public ResponseEntity<ApiResult<NicknameGenerateResponse>> generateNickname() {
        String nickname = nicknameGeneratorService.generateUniqueNickname();
        return ResponseEntity.ok(ApiResult.ok(NicknameGenerateResponse.of(nickname)));
    }
}
