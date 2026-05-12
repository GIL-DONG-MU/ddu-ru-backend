package com.dduru.gildongmu.journey.dto.request;

import jakarta.validation.constraints.Size;

public record JourneyPostUpdateRequest(
        @Size(max = 30)
        String title,

        @Size(max = 300)
        String content,

        String imageUrl
) {
}
