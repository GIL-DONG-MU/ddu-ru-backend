package com.dduru.gildongmu.survey.domain.enums;

import com.dduru.gildongmu.common.enums.CodedEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RhythmQuestion2 implements CodedEnum, AxisBinaryScore {
    PACK_EARLY(1, 0),
    PACK_LAST_MINUTE(2, 2);

    private final int code;
    private final int axisScorePoints;
}
