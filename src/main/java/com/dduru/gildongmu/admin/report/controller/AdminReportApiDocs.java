package com.dduru.gildongmu.admin.report.controller;

import com.dduru.gildongmu.admin.report.dto.request.AdminReportListRequest;
import com.dduru.gildongmu.admin.report.dto.request.AdminReportUpdateRequest;
import com.dduru.gildongmu.admin.report.dto.response.AdminReportListResponse;
import com.dduru.gildongmu.admin.report.dto.response.AdminReportResponse;
import com.dduru.gildongmu.common.annotation.ApiErrorResponses;
import com.dduru.gildongmu.common.dto.ApiResult;
import com.dduru.gildongmu.common.exception.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Admin Reports", description = "관리자 신고 API")
public interface AdminReportApiDocs {

    @Operation(summary = "신고 목록 조회", description = "신고 내역을 상태·페이지로 조회합니다. 상태를 생략하면 전체입니다.")
    @ApiResponse(responseCode = "200", description = "신고 목록 조회 성공")
    @ApiErrorResponses({
            ErrorCode.UNAUTHORIZED,
            ErrorCode.ACCESS_DENIED
    })
    ResponseEntity<ApiResult<AdminReportListResponse>> listReports(
            @ParameterObject AdminReportListRequest request,
            @ParameterObject @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    );

    @Operation(summary = "신고 처리", description = "신고 상태·처리 메모를 갱신하고 처리자를 기록합니다.")
    @ApiResponse(responseCode = "200", description = "신고 처리 성공")
    @ApiErrorResponses({
            ErrorCode.INVALID_INPUT_VALUE,
            ErrorCode.REPORT_NOT_FOUND,
            ErrorCode.USER_NOT_FOUND,
            ErrorCode.UNAUTHORIZED,
            ErrorCode.ACCESS_DENIED
    })
    ResponseEntity<ApiResult<AdminReportResponse>> updateReport(
            @Parameter(description = "신고 ID") Long reportId,
            @Parameter(hidden = true) Long reviewerId,
            @Valid @RequestBody AdminReportUpdateRequest request
    );
}
