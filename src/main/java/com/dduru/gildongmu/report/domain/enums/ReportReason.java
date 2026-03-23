package com.dduru.gildongmu.report.domain.enums;

import lombok.Getter;

@Getter
public enum ReportReason {
    SPAM_AD("스팸 또는 광고"),
    INAPPROPRIATE("부적절한 내용"),
    SCAM("사기 의심"),
    FALSE_INFO("허위 정보"),
    DUPLICATE("중복 게시물"),
    PRIVACY_EXPOSURE("개인정보 노출"),
    OTHER("기타");

    private final String description;

    ReportReason(String description) {
        this.description = description;
    }
}
