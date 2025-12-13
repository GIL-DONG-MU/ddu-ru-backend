package com.dduru.gildongmu.nickname.controller;

import com.dduru.gildongmu.common.dto.ApiResult;
import com.dduru.gildongmu.nickname.dto.NicknameGenerateRequest;
import com.dduru.gildongmu.nickname.dto.NicknameGenerateResponse;
import com.dduru.gildongmu.nickname.enums.NicknameTheme;
import com.dduru.gildongmu.nickname.service.NicknameGeneratorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 랜덤 닉네임 생성 API 컨트롤러
 */
@Validated
@RequiredArgsConstructor
@RequestMapping("/api/v1/nicknames")
@RestController
public class NicknameController implements NicknameApiDocs {

    private final NicknameGeneratorService nicknameGeneratorService;

    /**
     * 랜덤 닉네임 생성 (POST)
     * 상세한 옵션으로 닉네임을 생성합니다.
     */
    @Override
    @PostMapping("/generate")
    public ResponseEntity<ApiResult<NicknameGenerateResponse>> generateNicknames(
            @Valid @RequestBody NicknameGenerateRequest request
    ) {
        NicknameGenerateResponse response = nicknameGeneratorService.generateNicknames(request);
        return ResponseEntity.ok(ApiResult.ok(response));
    }

    /**
     * 테마별 랜덤 닉네임 생성 (GET)
     * 간편하게 테마와 개수만 지정하여 생성합니다.
     */
    @Override
    @GetMapping("/generate/{theme}")
    public ResponseEntity<ApiResult<NicknameGenerateResponse>> generateNicknamesByTheme(
            @PathVariable NicknameTheme theme,
            @RequestParam(defaultValue = "5") Integer count
    ) {
        NicknameGenerateRequest request = new NicknameGenerateRequest(count, theme, false);
        NicknameGenerateResponse response = nicknameGeneratorService.generateNicknames(request);
        return ResponseEntity.ok(ApiResult.ok(response));
    }

    /**
     * 단일 랜덤 닉네임 생성 (GET)
     * 중복되지 않는 유니크한 닉네임을 하나 생성합니다.
     */
    @Override
    @GetMapping("/generate/single")
    public ResponseEntity<ApiResult<NicknameGenerateResponse>> generateSingleNickname() {
        String nickname = nicknameGeneratorService.generateUniqueNickname(NicknameTheme.RANDOM);
        NicknameGenerateResponse response = NicknameGenerateResponse.of(List.of(nickname));
        return ResponseEntity.ok(ApiResult.ok(response));
    }

    /**
     * 사용 가능한 테마 목록 조회
     */
    @Override
    @GetMapping("/themes")
    public ResponseEntity<ApiResult<NicknameTheme[]>> getAvailableThemes() {
        return ResponseEntity.ok(ApiResult.ok(NicknameTheme.values()));
    }
}
