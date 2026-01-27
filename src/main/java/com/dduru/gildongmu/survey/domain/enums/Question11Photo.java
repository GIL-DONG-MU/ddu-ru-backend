package com.dduru.gildongmu.survey.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Question11Photo implements SurveyOptionSpec {
    LIFETIME_SHOT(1, "📸", "사진은 무조건 많이! 인생샷 남기기"),
    MATCH_COMPANION(2, "🙂", "흠... 상관 없음! 동행자 스타일에 맞출래"),
    EYES_ONLY(3, "👀", "사진보단 눈으로 담는 게 진짜지");

    private final int code;
    private final String icon;
    private final String description;
}
