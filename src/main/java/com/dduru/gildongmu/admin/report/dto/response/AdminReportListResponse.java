package com.dduru.gildongmu.admin.report.dto.response;

import com.dduru.gildongmu.report.domain.Report;
import org.springframework.data.domain.Page;

import java.util.List;

public record AdminReportListResponse(
        List<AdminReportResponse> content,
        long totalElements,
        int totalPages,
        int page,
        int size,
        boolean last
) {
    public static AdminReportListResponse from(Page<Report> page) {
        return new AdminReportListResponse(
                page.getContent().stream().map(AdminReportResponse::from).toList(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber(),
                page.getSize(),
                page.isLast()
        );
    }
}
