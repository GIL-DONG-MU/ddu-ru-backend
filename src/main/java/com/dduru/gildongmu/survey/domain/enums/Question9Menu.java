package com.dduru.gildongmu.survey.domain.enums;

import com.dduru.gildongmu.common.enums.CodedEnum;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum Question9Menu implements CodedEnum {
    SAFE(1, "안전한 메뉴"),
    CHECK_REVIEW(2, "후기 확인 후 결정"),
    CHALLENGE(3, "바로 도전");

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
