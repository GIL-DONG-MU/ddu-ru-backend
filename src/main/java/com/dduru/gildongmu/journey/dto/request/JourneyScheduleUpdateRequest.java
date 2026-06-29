package com.dduru.gildongmu.journey.dto.request;

import com.dduru.gildongmu.journey.domain.enums.ScheduleCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;

public record JourneyScheduleUpdateRequest(
        @Schema(description = "변경할 일정 제목. 미전달 시 기존 값을 유지합니다.", example = "성산일출봉", nullable = true)
        @Size(max = 30)
        String title,

        @Schema(description = "변경할 일정 카테고리. 미전달 시 기존 값을 유지합니다.", example = "SIGHTSEEING", nullable = true)
        ScheduleCategory category,

        @Schema(description = "변경할 여행 시작일 기준 N일차. 미전달 시 기존 값을 유지합니다.", example = "1", nullable = true)
        @Min(0)
        Integer dayOffset,

        @Schema(description = "변경할 시작 시간. clearStartTime=true이면 무시되고 기존 시간이 제거됩니다.", example = "09:30", nullable = true)
        LocalTime startTime,

        @Schema(description = "시작 시간 제거 여부. true이면 startTime을 null로 저장합니다.", example = "false", nullable = true)
        Boolean clearStartTime,

        @Schema(description = "변경할 종료 시간. clearEndTime=true이면 무시되고 기존 시간이 제거됩니다.", example = "11:00", nullable = true)
        LocalTime endTime,

        @Schema(description = "종료 시간 제거 여부. true이면 endTime을 null로 저장합니다.", example = "false", nullable = true)
        Boolean clearEndTime,

        @Schema(description = "변경할 장소명. 미전달 시 기존 값을 유지합니다.", example = "성산일출봉", nullable = true)
        @Size(max = 30)
        String placeName,

        @Schema(description = "변경할 일정 메모. 미전달 시 기존 값을 유지합니다.", example = "일출 시간 확인하기", nullable = true)
        @Size(max = 100)
        String memo
) {
    public JourneyScheduleUpdateRequest {
        if (title != null) title = title.strip();
        if (placeName != null) placeName = placeName.strip();
        if (memo != null) memo = memo.strip();
    }
}
