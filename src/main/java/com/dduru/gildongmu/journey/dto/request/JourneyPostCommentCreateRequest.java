package com.dduru.gildongmu.journey.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record JourneyPostCommentCreateRequest(
        @NotBlank
        @Size(max = 300)
        String content
) {
    public JourneyPostCommentCreateRequest {
        if (content != null) content = content.strip();
    }
}
