package com.dduru.gildongmu.report.domain.enums;

import lombok.Getter;

@Getter
public enum ReportStatus {
    RECEIVED("접수됨"),
    IN_REVIEW("검토중"),
    REJECTED("거부됨"),
    RESOLVED("완료");

    private final String description;

    ReportStatus(String description) {
        this.description = description;
    }
}
