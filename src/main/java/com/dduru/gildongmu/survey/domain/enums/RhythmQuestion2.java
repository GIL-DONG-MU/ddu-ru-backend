package com.dduru.gildongmu.survey.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RhythmQuestion2 implements SurveyOptionSpec, AxisBinaryScore {
    PACK_EARLY(1, 0, "📋", "캐리어는 3일 전부터 오픈… 미리미리 완벽 준비"),
    PACK_LAST_MINUTE(2, 2, "🔥", "여행 전날 밤? 그때 몰아서 싸지 뭐~");

    private final int code;
    private final int axisScorePoints;
    private final String icon;
    private final String description;
}
