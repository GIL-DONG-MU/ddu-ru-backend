package com.dduru.gildongmu.journey.dto.request;

import jakarta.validation.constraints.Size;

public record JourneyPostCommentUpdateRequest(
        @Size(max = 300)
        String content
) {
}
