package com.dduru.gildongmu.survey.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record SurveyRequest(
        @NotNull(message = "여행 리듬 Q1은 필수입니다.")
        Integer rhythmQ1,

        @NotNull(message = "여행 리듬 Q2는 필수입니다.")
        Integer rhythmQ2,

        @NotNull(message = "여행 리듬 Q3은 필수입니다.")
        Integer rhythmQ3,

        @NotNull(message = "활동 취향은 필수입니다.")
        @Size(min = 1, max = 3, message = "활동 취향은 1~3개까지 선택할 수 있습니다.")
        List<Integer> activityTags,

        @NotNull(message = "소비 가치 Q1은 필수입니다.")
        Integer consumptionQ1,

        @NotNull(message = "소비 가치 Q2는 필수입니다.")
        Integer consumptionQ2,

        @NotNull(message = "소비 가치 Q3은 필수입니다.")
        Integer consumptionQ3,

        @NotNull(message = "활동 에너지 Q1은 필수입니다.")
        Integer energyQ1,

        @NotNull(message = "활동 에너지 Q2는 필수입니다.")
        Integer energyQ2,

        @NotNull(message = "활동 에너지 Q3은 필수입니다.")
        Integer energyQ3,

        @NotNull(message = "의사결정 Q1은 필수입니다.")
        Integer decisionQ1,

        @NotNull(message = "의사결정 Q2는 필수입니다.")
        Integer decisionQ2,

        @NotNull(message = "의사결정 Q3은 필수입니다.")
        Integer decisionQ3,

        @NotNull(message = "기록 스타일은 필수입니다.")
        Integer recordStyle
) {
}
