package com.dduru.gildongmu.home.controller;

import com.dduru.gildongmu.common.dto.ApiResult;
import com.dduru.gildongmu.home.dto.response.HomeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Home", description = "홈 API")
public interface HomeApiDocs {

    @Operation(
            summary = "홈 화면 조회",
            description = "홈 화면에 필요한 데이터를 조회합니다. 비회원도 호출할 수 있고, JWT가 있으면 사용자 상태에 따라 개인화 섹션을 함께 반환합니다."
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    ResponseEntity<ApiResult<HomeResponse>> retrieveHome(
            @Parameter(hidden = true) Long userId
    );
}
