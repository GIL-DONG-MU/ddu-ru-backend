package com.dduru.gildongmu.recommendation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "여행 선호 설정 조회 응답")
public record TravelPreferenceResponse(
        @Schema(description = "여행지 선호 목록")
        List<DestinationPreferenceResponse> destinationPreferences,
        @Schema(description = "여행 가능 날짜 목록")
        List<AvailableDateResponse> availableDates
) {
}
