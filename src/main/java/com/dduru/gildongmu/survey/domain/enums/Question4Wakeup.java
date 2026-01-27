package com.dduru.gildongmu.survey.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Question4Wakeup implements SurveyOptionSpec {
    EARLY(1, "🌞", "일찍 일어나서 알차게 돌아다니자!"),
    RELAXED(2, "🐌", "서두르기보단 느긋하게 돌아다니자~");

    private final int code;
    private final String icon;
    private final String description;
}
