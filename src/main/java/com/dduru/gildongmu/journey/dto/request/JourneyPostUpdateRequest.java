package com.dduru.gildongmu.journey.dto.request;

import jakarta.validation.constraints.Size;

public record JourneyPostUpdateRequest(
        @Size(max = 300)
        String content,

        String imageUrl
) {
    public JourneyPostUpdateRequest {
        if (content != null) content = content.strip();
        if (imageUrl != null) imageUrl = imageUrl.strip();
    }
}
