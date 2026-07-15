package com.dduru.gildongmu.destination.dto;

import com.dduru.gildongmu.destination.domain.Destination;
import com.dduru.gildongmu.recommendation.domain.enums.RecommendationDestinationPreferenceType;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "여행지 선호 검색 결과")
public record DestinationPreferenceSearchResponse(
        @Schema(description = "선호 타입 (COUNTRY: 국가, CITY: 도시)", example = "CITY")
        RecommendationDestinationPreferenceType type,
        @Schema(description = "국가 코드", example = "JP")
        String countryCode,
        @Schema(description = "국가명", example = "일본")
        String countryName,
        @Schema(description = "도시 ID (CITY 타입만)", example = "1")
        Long destinationId,
        @Schema(description = "도시명 (CITY 타입만)", example = "도쿄")
        String city
) {
    public static DestinationPreferenceSearchResponse country(Destination d) {
        return new DestinationPreferenceSearchResponse(
                RecommendationDestinationPreferenceType.COUNTRY,
                d.getCountryCode(),
                d.getCountryName(),
                null,
                null
        );
    }

    public static DestinationPreferenceSearchResponse city(Destination d) {
        return new DestinationPreferenceSearchResponse(
                RecommendationDestinationPreferenceType.CITY,
                d.getCountryCode(),
                d.getCountryName(),
                d.getId(),
                d.getCity()
        );
    }
}
