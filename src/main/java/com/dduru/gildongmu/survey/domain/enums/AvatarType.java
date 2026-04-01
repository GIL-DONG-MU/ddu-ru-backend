package com.dduru.gildongmu.survey.domain.enums;

import com.dduru.gildongmu.common.enums.CodedEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AvatarType implements CodedEnum {
    TTUR_POGUNI(1, "뚜르 포근이"),
    TTUR_MOOD(2, "뚜르 무드"),
    TTUR_MALLANGI(3, "뚜르 말랑이"),
    TTUR_SWEET(4, "뚜르 스윗"),
    TTUR_POPO(5, "뚜르 포포"),
    TTUR_SPARKLE(6, "뚜르 스파클"),
    TTUR_GLIMMING(7, "뚜르 글리밍"),
    TTUR_PADO(8, "뚜르 파도"),
    TTUR_DASHI(9, "뚜르 대시"),
    TTUR_FLARE(10, "뚜르 플레어"),
    TTUR_BOUNCY(11, "뚜르 바운시"),
    TTUR_PEPPI(12, "뚜르 페피"),
    TTUR_JETTI(13, "뚜르 제티"),
    TTUR_BLAZE(14, "뚜르 블레이즈"),
    TTUR_VIVID(15, "뚜르 비비드"),
    TTUR_SURGE(16, "뚜르 서지");

    private final int code;
    private final String text;

}
