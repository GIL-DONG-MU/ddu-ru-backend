package com.dduru.gildongmu.journey.dto.request;

import jakarta.validation.constraints.Size;

import java.util.List;

public record JourneyPostUpdateRequest(
        @Size(max = 300)
        String content,

        @Size(max = 4)
        List<String> imageUrls
) {
    public JourneyPostUpdateRequest {
        if (content != null) content = content.strip();
    }
}
