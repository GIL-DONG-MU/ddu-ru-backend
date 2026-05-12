package com.dduru.gildongmu.journey.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record JourneyPostCreateRequest(
        @NotBlank
        @Size(max = 30)
        String title,

        @NotBlank
        @Size(max = 300)
        String content,

        String imageUrl
) {
}
