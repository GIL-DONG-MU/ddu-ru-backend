package com.dduru.gildongmu.post.dto;

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
}
