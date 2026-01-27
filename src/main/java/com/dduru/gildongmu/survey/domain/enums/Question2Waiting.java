package com.dduru.gildongmu.survey.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Question2Waiting implements SurveyOptionSpec {
    WAIT(1, "😤", "이왕이면 기다려서라도 먹어야해!"),
    MOVE_ELSEWHERE(2, "😄", "기다리기는 좀... 편하게 주변에서 먹자");

    private final int code;
    private final String icon;
    private final String description;
}
