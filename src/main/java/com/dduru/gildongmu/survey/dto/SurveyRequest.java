package com.dduru.gildongmu.survey.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record SurveyRequest(
        @NotNull(message = "Q1 이동수단은 필수입니다.")
        Integer q1,

        @NotNull(message = "Q2 웨이팅은 필수입니다.")
        Integer q2,

        @NotNull(message = "Q3 숙소는 필수입니다.")
        Integer q3,

        @NotNull(message = "Q4 기상시간은 필수입니다.")
        Integer q4,

        @NotNull(message = "Q5 경비관리는 필수입니다.")
        Integer q5,

        @NotNull(message = "Q6 소비태도는 필수입니다.")
        Integer q6,

        @NotNull(message = "Q7 선호활동은 필수입니다.")
        @Size(min = 3, max = 3, message = "Q7 선호활동은 정확히 3개를 선택해야 합니다.")
        List<Integer> q7,

        @NotNull(message = "Q8 계획성은 필수입니다.")
        Integer q8,

        @NotNull(message = "Q9 낯선메뉴는 필수입니다.")
        Integer q9,

        @NotNull(message = "Q10 동행제안은 필수입니다.")
        Integer q10,

        @NotNull(message = "Q11 사진은 필수입니다.")
        Integer q11
) {
}
