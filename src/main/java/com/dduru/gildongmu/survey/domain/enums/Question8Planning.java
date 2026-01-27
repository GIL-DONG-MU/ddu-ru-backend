package com.dduru.gildongmu.survey.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Question8Planning implements SurveyOptionSpec {
    DETAILED(1, "📅", "시간별 디테일한 일정표!"),
    FLEXIBLE(2, "📘", "큰 틀만 잡고 상황 따라 융통성 있게 조정"),
    ON_SITE(3, "📍", "가고 싶은 곳만 정하고 현장에서 결정!");

    private final int code;
    private final String icon;
    private final String description;
}
