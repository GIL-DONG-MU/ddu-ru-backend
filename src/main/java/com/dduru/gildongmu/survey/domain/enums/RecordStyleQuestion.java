package com.dduru.gildongmu.survey.domain.enums;

import com.dduru.gildongmu.common.enums.CodedEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RecordStyleQuestion implements CodedEnum {
    EYES_FIRST(1, RecordStyleType.A),
    SHOOT_NOW(2, RecordStyleType.B);

    private final int code;
    private final RecordStyleType styleType;
}
