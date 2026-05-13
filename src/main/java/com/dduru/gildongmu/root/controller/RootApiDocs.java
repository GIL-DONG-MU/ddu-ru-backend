package com.dduru.gildongmu.root.controller;

import com.dduru.gildongmu.common.dto.ApiResult;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface RootApiDocs {

    @Hidden
    @Operation(summary = "웰컴 메시지", description = "서비스 기본 정보를 json형식으로 반환합니다.")
    @ApiResponse(responseCode = "200", description = "성공")
    ResponseEntity<ApiResult<Map<String, Object>>> welcome();
}
