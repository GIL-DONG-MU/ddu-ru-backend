package com.dduru.gildongmu.destination.dto;

import com.dduru.gildongmu.destination.domain.Destination;
import lombok.Builder;

@Builder
public record DestinationInfo(
        Long id,
        String countryCode,
        String countryName,
        String city,
        String region

) {
    public static DestinationInfo from(Destination destination) {
        return DestinationInfo.builder()
                .id(destination.getId())
                .countryCode(destination.getCountryCode())
                .countryName(destination.getCountryName())
                .city(destination.getCity())
                .region(destination.getRegion())
                .build();
    }
}
