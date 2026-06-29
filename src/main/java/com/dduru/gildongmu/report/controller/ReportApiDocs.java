package com.dduru.gildongmu.report.controller;

import com.dduru.gildongmu.common.annotation.ApiErrorResponses;
import com.dduru.gildongmu.common.dto.ApiResult;
import com.dduru.gildongmu.common.exception.ErrorCode;
import com.dduru.gildongmu.report.dto.request.ReportCreateRequest;
import com.dduru.gildongmu.report.dto.response.ReportCreateResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;

@Tag(name = "Reports", description = "게시글 신고 API")
@SecurityRequirement(name = "JWT")
public interface ReportApiDocs {

    @Operation(summary = "게시글 신고", description = "게시글을 신고합니다.")
    @ApiResponse(responseCode = "201", description = "신고 접수 성공")
    @ApiErrorResponses({
            ErrorCode.INVALID_INPUT_VALUE,
            ErrorCode.POST_NOT_FOUND,
            ErrorCode.SELF_POST_REPORT_NOT_ALLOWED,
            ErrorCode.DUPLICATE_POST_REPORT,
            ErrorCode.USER_NOT_FOUND,
            ErrorCode.UNAUTHORIZED
    })
    ResponseEntity<ApiResult<ReportCreateResponse>> createReport(
            @Parameter(description = "게시글 ID") Long postId,
            @Parameter(hidden = true) Long userId,
            @Valid ReportCreateRequest request
    );
}
