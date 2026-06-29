package com.dduru.gildongmu.journey.dto.request;

import com.dduru.gildongmu.journey.domain.enums.ScheduleCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;

public record JourneyScheduleCreateRequest(
        @Schema(description = "일정 제목. 앞뒤 공백은 제거됩니다.", example = "성산일출봉")
        @NotBlank
        @Size(max = 30)
        String title,

        @Schema(description = "일정 카테고리", example = "SIGHTSEEING", nullable = true)
        ScheduleCategory category,

        @Schema(description = "여행 시작일 기준 N일차. 0은 첫째 날입니다.", example = "0")
        @NotNull
        @Min(0)
        Integer dayOffset,

        @Schema(description = "일정 시작 시간. 종일 일정이면 생략할 수 있습니다.", example = "09:30", nullable = true)
        LocalTime startTime,

        @Schema(description = "일정 종료 시간. startTime이 있을 때만 사용할 수 있습니다.", example = "11:00", nullable = true)
        LocalTime endTime,

        @Schema(description = "장소명. 앞뒤 공백은 제거됩니다.", example = "성산일출봉")
        @NotBlank
        @Size(max = 30)
        String placeName,

        @Schema(description = "일정 메모. 선택 입력이며 최대 100자입니다.", example = "일출 시간 확인하기", nullable = true)
        @Size(max = 100)
        String memo
) {
    public JourneyScheduleCreateRequest {
        if (title != null) title = title.strip();
        if (placeName != null) placeName = placeName.strip();
        if (memo != null) memo = memo.strip();
    }
}
