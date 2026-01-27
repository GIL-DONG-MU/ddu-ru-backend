package com.dduru.gildongmu.survey.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Question5Expense implements SurveyOptionSpec {
    EACH_PAYS(1, "💰", "꼼꼼하게 각자 결제!"),
    POOLED(2, "🐷", "한 통장에 모아 함께 쓰기!");

    private final int code;
    private final String icon;
    private final String description;
}
