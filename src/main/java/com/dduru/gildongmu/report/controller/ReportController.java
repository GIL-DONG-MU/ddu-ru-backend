package com.dduru.gildongmu.report.controller;

import com.dduru.gildongmu.common.annotation.CurrentUser;
import com.dduru.gildongmu.common.dto.ApiResult;
import com.dduru.gildongmu.report.dto.request.ReportCreateRequest;
import com.dduru.gildongmu.report.dto.response.ReportCreateResponse;
import com.dduru.gildongmu.report.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ReportController implements ReportApiDocs {

    private final ReportService reportService;

    @Override
    @PostMapping("/posts/{postId}/reports")
    public ResponseEntity<ApiResult<ReportCreateResponse>> createReport(
            @PathVariable Long postId,
            @CurrentUser Long userId,
            @Valid @RequestBody ReportCreateRequest request
    ) {
        ReportCreateResponse response = reportService.create(postId, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResult.created(response));
    }
}
