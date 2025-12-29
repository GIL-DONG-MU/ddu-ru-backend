package com.dduru.gildongmu.survey.domain.enums;

import com.dduru.gildongmu.common.enums.CodedEnum;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum Question11Photo implements CodedEnum {
    LIFETIME_SHOT(1, "인생샷"),
    MATCH_COMPANION(2, "동행자에 맞춤"),
    EYES_ONLY(3, "눈으로 담기");

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
