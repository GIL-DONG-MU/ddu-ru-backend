package com.dduru.gildongmu.survey.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RhythmQuestion1 implements SurveyOptionSpec, AxisBinaryScore {
    PLANNED_ROUTE(1, 0, "✨", "최단 거리가 최고! 정해진 루트대로 다음 일정으로"),
    IMPULSE_SIDE_TRIP(2, 2, "🌈", "어? 저기 예쁜데? 가는 길에 궁금한 곳이 생기면 일단 들렀다 가자!");

    private final int code;
    private final int axisScorePoints;
    private final String icon;
    private final String description;
}
