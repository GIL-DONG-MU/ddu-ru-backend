package com.dduru.gildongmu.verification.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record VerificationData(
        String code,
        String phone,
        @JsonProperty("count")
        Integer count,
        String status
) {
}
