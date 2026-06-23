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
            summary = "홈 초기 구성 조회",
            description = """
                    홈 화면 진입에 필요한 사용자 상태와 섹션 호출 정보를 조회합니다.

                    실제 섹션 데이터는 sections[].endpoint로 내려가는 API를 클라이언트가 별도로 호출합니다.
                    모든 홈 섹션 데이터는 /api/v1/home/* 홈 전용 API로 조회합니다.
                    enabled=false인 섹션은 클라이언트가 호출하지 않고 disabledReason을 기준으로 UI를 처리합니다.
                    """
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    ResponseEntity<ApiResult<HomeResponse>> retrieveHome(
            @Parameter(hidden = true) Long userId
    );
}
