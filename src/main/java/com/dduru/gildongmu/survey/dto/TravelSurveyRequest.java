package com.dduru.gildongmu.survey.dto;

import com.dduru.gildongmu.survey.validation.ValidInterestList;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.util.List;

public record TravelSurveyRequest(
        @NotBlank
        @Pattern(regexp = "PLANNER|FREE", message = "유효하지 않은 계획 스타일입니다")
        String planStyle,

        @NotBlank
        @Pattern(regexp = "WAIT|NEARBY", message = "유효하지 않은 맛집 대기 스타일입니다")
        String tastingStyle,

        @NotBlank
        @Pattern(regexp = "HOTEL|JUST_SLEEP", message = "유효하지 않은 숙소 스타일입니다")
        String stayStyle,

        @NotBlank
        @Pattern(regexp = "EACH_PAYS|POOLED", message = "유효하지 않은 정산 스타일입니다")
        String expenseStyle,

        @NotBlank
        @Pattern(regexp = "WALK_BUS|TAXI", message = "유효하지 않은 이동 수단 스타일입니다")
        String moveStyle,

        @NotBlank
        @Pattern(regexp = "SPLURGE|SAVER", message = "유효하지 않은 소비 스타일입니다")
        String spendStyle,

        @NotBlank
        @Pattern(regexp = "PHOTO|EYES", message = "유효하지 않은 기록 스타일입니다")
        String captureStyle,

        @NotBlank
        @Pattern(regexp = "EARLY_FULL|RELAXED", message = "유효하지 않은 일정 템포 스타일입니다")
        String paceStyle,

        @Schema(description = "관심사 리스트", example = "[\"SIGHTSEEING\", \"EXHIBITION\", \"NATURE\"]")
        @ValidInterestList()
        List<String> interests
) {
}
