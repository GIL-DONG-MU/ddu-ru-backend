package com.dduru.gildongmu.survey.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Question1Transport implements SurveyOptionSpec {
    WALK_BUS(1, "🚶", "여행은 낭만이지! 걷거나 버스"),
    TAXI(2, "🚕", "편한게 최고~ 택시 타는 거 어때?");

    private final int code;
    private final String icon;
    private final String description;
}
