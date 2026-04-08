package com.dduru.gildongmu.survey.domain.enums;

import com.dduru.gildongmu.common.enums.CodedEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ActivityTag implements CodedEnum {
    SIGHTSEEING(1),
    EXHIBITION(2),
    NATURE(3),
    FOOD(4),
    SHOPPING(5),
    LEISURE(6),
    ACTIVITY(7),
    AMUSEMENT_PARK(8),
    FESTIVAL(9);

    private final int code;
}
