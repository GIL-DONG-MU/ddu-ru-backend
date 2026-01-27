package com.dduru.gildongmu.survey.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Question10Companion implements SurveyOptionSpec {
    WELCOME(1, "🤗", "새 친구 생기면 좋지! 완전 환영!"),
    SITUATIONAL(2, "🙂", "어떤 사람일까? 잘 맞으면 재밌을 듯!"),
    US_ONLY(3, "🙅‍♂️", "우리끼리 여행이 좋아! 정중히 거절!");

    private final int code;
    private final String icon;
    private final String description;
}
