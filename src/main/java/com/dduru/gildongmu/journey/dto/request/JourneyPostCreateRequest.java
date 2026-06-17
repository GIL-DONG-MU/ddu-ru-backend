package com.dduru.gildongmu.journey.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record JourneyPostCreateRequest(
        @NotBlank
        @Size(max = 300)
        String content,

        String imageUrl
) {
    public JourneyPostCreateRequest {
        if (content != null) content = content.strip();
        if (imageUrl != null) imageUrl = imageUrl.strip();
    }
}
