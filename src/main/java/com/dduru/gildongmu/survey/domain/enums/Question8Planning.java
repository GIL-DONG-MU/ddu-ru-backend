package com.dduru.gildongmu.survey.domain.enums;

import com.dduru.gildongmu.common.enums.CodedEnum;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum Question8Planning implements CodedEnum {
    DETAILED(1, "디테일한 계획"),
    FLEXIBLE(2, "융통성 있는 계획"),
    ON_SITE(3, "현장 결정");

    private final int code;
    private final String text;

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getText() {
        return text;
    }
}
