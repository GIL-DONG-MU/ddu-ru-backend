package com.dduru.gildongmu.survey.domain.enums;

import com.dduru.gildongmu.common.enums.CodedEnum;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum Question2Waiting implements CodedEnum {
    WAIT(1, "기다림"),
    MOVE_ELSEWHERE(2, "다른 곳 이동");

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
