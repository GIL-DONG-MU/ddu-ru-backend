package com.dduru.gildongmu.survey.domain.enums;

import com.dduru.gildongmu.common.enums.CodedEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RhythmQuestion3 implements CodedEnum, AxisBinaryScore {
    ROUTE_TIME_SET(1, 0),
    ROUGH_LIST_ONLY(2, 2);

    private final int code;
    private final int axisScorePoints;
}
