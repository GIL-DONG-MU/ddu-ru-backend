package com.dduru.gildongmu.survey.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Question3Stay implements SurveyOptionSpec {
    HOTEL(1, "🏨", "잠은 갖춰진 곳에서 자야지"),
    JUST_SLEEP(2, "😴", "잠만 잘 수 있으면 OK!");

    private final int code;
    private final String icon;
    private final String description;
}
