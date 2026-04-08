package com.dduru.gildongmu.survey.domain.enums;

import com.dduru.gildongmu.common.enums.CodedEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RhythmQuestion1 implements CodedEnum, AxisBinaryScore {
    PLANNED_ROUTE(1, 0),
    IMPULSE_SIDE_TRIP(2, 2);

    private final int code;
    private final int axisScorePoints;
}
