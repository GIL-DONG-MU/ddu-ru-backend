package com.dduru.gildongmu.survey.domain.enums;

import com.dduru.gildongmu.common.enums.CodedEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PaceStyle implements CodedEnum {
    EARLY_FULL(1, "아침 일찍 꽉 채우기"),
    RELAXED(2, "여유롭게");

    private final int code;
    private final String text;
}
