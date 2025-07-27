package com.dduru.gildongmu.post.dto;

import com.dduru.gildongmu.post.enums.Destination;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DestinationInfo {
    private Long id;
    private String countryCode;
    private String countryName;
    private String city;
    private String region;

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
