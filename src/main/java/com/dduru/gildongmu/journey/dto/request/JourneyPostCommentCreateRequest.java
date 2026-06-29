package com.dduru.gildongmu.journey.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record JourneyPostCommentCreateRequest(
        @Schema(description = "댓글 내용. 앞뒤 공백은 제거되며 최대 300자입니다.", example = "좋아요, 이 일정으로 가요.")
        @NotBlank
        @Size(max = 300)
        String content
) {
    public JourneyPostCommentCreateRequest {
        if (content != null) content = content.strip();
    }
}
