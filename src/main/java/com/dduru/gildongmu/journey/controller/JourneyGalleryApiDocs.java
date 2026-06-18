package com.dduru.gildongmu.journey.controller;

import com.dduru.gildongmu.common.annotation.ApiErrorResponses;
import com.dduru.gildongmu.common.dto.ApiResult;
import com.dduru.gildongmu.common.exception.ErrorCode;
import com.dduru.gildongmu.journey.dto.request.JourneyGalleryRequest;
import com.dduru.gildongmu.journey.dto.response.JourneyGalleryResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;

@Tag(name = "Journey Gallery", description = "나의 여정 갤러리 API")
@SecurityRequirement(name = "JWT")
public interface JourneyGalleryApiDocs {

    @Operation(
            summary = "나의 여정 갤러리 조회",
            description = """
                    active journey member가 여정 내 게시글 이미지를 최신 게시글 순으로 통합 조회합니다.
                    이미지 단위 커서 페이지네이션을 사용하며, size는 이미지 수 기준입니다.
                    커서는 (cursorPostId, cursorSortOrder) 복합 커서이며, 응답의 nextCursor 값을 그대로 사용하면 됩니다.
                    size는 최대 50이며 초과 시 50으로 클램프됩니다.
                    """
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiErrorResponses({
            ErrorCode.UNAUTHORIZED,
            ErrorCode.JOURNEY_ACCESS_DENIED
    })
    ResponseEntity<ApiResult<JourneyGalleryResponse>> retrieveGallery(
            @Parameter(description = "여정 ID") Long journeyId,
            @Parameter(hidden = true) Long userId,
            @Valid @ParameterObject JourneyGalleryRequest request
    );
}
