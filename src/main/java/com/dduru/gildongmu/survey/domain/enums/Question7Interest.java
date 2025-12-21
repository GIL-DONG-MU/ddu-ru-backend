package com.dduru.gildongmu.survey.domain.enums;

import com.dduru.gildongmu.common.enums.CodedEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Question7Interest implements CodedEnum {
    SIGHTSEEING(1, "관광"),
    EXHIBITION(2, "관람"),
    NATURE(3, "자연 탐방"),
    FOOD(4, "맛집 탐방"),
    SHOPPING(5, "쇼핑"),
    RESORT(6, "휴양"),
    ACTIVITY(7, "액티비티"),
    THEME_PARK(8, "놀이공원"),
    FESTIVAL(9, "페스티벌");

    private final int code;
    private final String text;
}
