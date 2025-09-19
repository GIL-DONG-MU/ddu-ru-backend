package com.dduru.gildongmu.survey.domain.enums;

import com.dduru.gildongmu.common.enums.CodedEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PlanStyle implements CodedEnum {
    PLANNER(1, "경로/맛집/시간까지"),
    FREE(2, "무계획의 즐거움");

    private final int code;
    private final String text;
}
