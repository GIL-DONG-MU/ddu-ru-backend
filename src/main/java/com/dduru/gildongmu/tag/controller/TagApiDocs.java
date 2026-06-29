package com.dduru.gildongmu.tag.controller;

import com.dduru.gildongmu.common.annotation.ApiErrorResponses;
import com.dduru.gildongmu.common.dto.ApiResult;
import com.dduru.gildongmu.common.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(name = "Tags", description = "게시글 태그 API")
@SecurityRequirement(name = "JWT")
public interface TagApiDocs {

    @Operation(
            summary = "인기 태그 목록 조회",
            description = "호스트방 작성 시 추천으로 보여줄 인기 태그 5개를 반환합니다."
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiErrorResponses({ErrorCode.UNAUTHORIZED})
    ResponseEntity<ApiResult<List<String>>> getPopularTags();
}
