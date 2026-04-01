package com.dduru.gildongmu.survey.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RecordStyleQuestion implements SurveyOptionSpec {
    EYES_FIRST(1, "👀", "카메라는 잠시 넣어두고… 눈에 먼저 저장"),
    SHOOT_NOW(2, "📸", "잠깐만!! 이건 인생샷 각이다. 바로 찍어야지");

    private final int code;
    private final String icon;
    private final String description;
}
