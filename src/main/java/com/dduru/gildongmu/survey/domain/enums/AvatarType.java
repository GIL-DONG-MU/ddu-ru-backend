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
    TTUR_PADO(8, "뚜르 파도");

    private final int code;
    private final String text;

    public String getDescription() {
        return switch (this) {
            case TTUR_POGUNI -> "안정형 가성비 조용러 (내 페이스대로 차분히)";
            case TTUR_MOOD -> "안정형 플랙스 감성러 (분위기와 여유로운 힐링)";
            case TTUR_MALLANGI -> "안정형 가성비 배려러 (함께 가며 챙겨주는 다정함)";
            case TTUR_SWEET -> "안정형 플랙스 미식 힐링러 (다같이 맛있는 미식 힐링)";
            case TTUR_POPO -> "모험형 가성비 실속러 (알뜰하고 영리한 실속 탐험)";
            case TTUR_SPARKLE -> "모험형 플랙스 경험러 (즉흥적이고 화려한 경험)";
            case TTUR_GLIMMING -> "모험형 가성비 미식 모험러 (북적이는 로컬 맛집 탐방)";
            case TTUR_PADO -> "모험형 플랙스 인싸러 (에너지 넘치는 즉흥 끝판왕)";
        };
    }
}
