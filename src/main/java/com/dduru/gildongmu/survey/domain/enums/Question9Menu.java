package com.dduru.gildongmu.survey.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Question9Menu implements SurveyOptionSpec {
    SAFE(1, "🍜", "음... 원래 고른 메뉴가 더 안전하지!"),
    CHECK_REVIEW(2, "🔎", "후기부터 확인해야지, 구글맵 출동!"),
    CHALLENGE(3, "🍽️", "이럴때 아니면 언제? 바로 가야지!!");

    private final int code;
    private final String icon;
    private final String description;
}
