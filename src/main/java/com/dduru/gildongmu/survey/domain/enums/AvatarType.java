package com.dduru.gildongmu.survey.domain.enums;

import com.dduru.gildongmu.common.enums.CodedEnum;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum AvatarType implements CodedEnum {
    TTUR_POGUNI(1, "뚜르 포근이"),
    TTUR_MOOD(2, "뚜르 무드"),
    TTUR_MALLANGI(3, "뚜르 말랑이"),
    TTUR_SWEET(4, "뚜르 스윗"),
    TTUR_POPO(5, "뚜르 포포"),
    TTUR_SPARKLE(6, "뚜르 스파클"),
    TTUR_GLIMMING(7, "뚜르 글리밍"),
    TTUR_PADO(8, "뚜르 파도");

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
