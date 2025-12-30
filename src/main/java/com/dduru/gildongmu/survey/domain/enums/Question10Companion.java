package com.dduru.gildongmu.survey.domain.enums;

import com.dduru.gildongmu.common.enums.CodedEnum;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum Question10Companion implements CodedEnum {
    WELCOME(1, "완전 환영"),
    SITUATIONAL(2, "상황 봐서"),
    US_ONLY(3, "우리끼리만");

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
