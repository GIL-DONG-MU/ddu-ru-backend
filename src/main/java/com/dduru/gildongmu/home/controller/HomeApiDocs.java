package com.dduru.gildongmu.home.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.Map;

@Tag(name = "Home", description = "홈 API")
public interface HomeApiDocs {

    @Operation(summary = "웰컴 메시지", description = "서비스 기본 정보를 json형식으로 반환합니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    ResponseEntity<Map<String, Object>> welcome();
}
