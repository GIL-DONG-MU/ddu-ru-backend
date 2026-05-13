package com.dduru.gildongmu.home.controller;

import com.dduru.gildongmu.common.annotation.ApiErrorResponses;
import com.dduru.gildongmu.common.dto.ApiResult;
import com.dduru.gildongmu.common.exception.ErrorCode;
import com.dduru.gildongmu.home.dto.response.HomeResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Home", description = "홈 API")
public interface HomeApiDocs {

    @Operation(
            summary = "홈 화면 조회",
            description = "홈 화면에 필요한 다가오는 여행, 인기 여행지, 추천 메이트, 슈퍼호스트, 추천 여행 목록을 조회합니다."
    )
    @SecurityRequirement(name = "JWT")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiErrorResponses({
            ErrorCode.UNAUTHORIZED
    })
    ResponseEntity<ApiResult<HomeResponse>> retrieveHome(
            @Parameter(hidden = true) Long userId
    );
}
