package com.dduru.gildongmu.destination.dto;

import com.dduru.gildongmu.destination.domain.Destination;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "여행지 정보")
public record DestinationInfo(
        @Schema(description = "여행지 ID", example = "1")
        Long id,
        @Schema(description = "국가 코드", example = "KR")
        String countryCode,
        @Schema(description = "국가명", example = "대한민국")
        String countryName,
        @Schema(description = "도시명", example = "제주도")
        String city,
        @Schema(description = "여행지 대표 이미지 URL", example = "https://cdn.example.com/destinations/jeju.jpg")
        String image
) {
    public static DestinationInfo from(Destination destination) {
        return DestinationInfo.builder()
                .id(destination.getId())
                .countryCode(destination.getCountryCode())
                .countryName(destination.getCountryName())
                .city(destination.getCity())
                .image(destination.getImage())
                .build();
    }
}
