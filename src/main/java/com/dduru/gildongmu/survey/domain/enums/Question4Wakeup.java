package com.dduru.gildongmu.survey.domain.enums;

import com.dduru.gildongmu.common.enums.CodedEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Question4Wakeup implements CodedEnum {
    EARLY(1, "일찍 기상"),
    RELAXED(2, "느긋하게 기상");

    private final int code;
    private final String text;
}
