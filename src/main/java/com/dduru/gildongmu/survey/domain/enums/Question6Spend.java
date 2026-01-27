package com.dduru.gildongmu.survey.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Question6Spend implements SurveyOptionSpec {
    SPLURGE(1, "🤑", "여행갔으면 써야지!"),
    SAVE(2, "😊", "아무래도 가성비가 최고지~");

    private final int code;
    private final String icon;
    private final String description;
}
