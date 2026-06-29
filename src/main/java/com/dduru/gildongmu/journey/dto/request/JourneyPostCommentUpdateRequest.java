package com.dduru.gildongmu.journey.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

public record JourneyPostCommentUpdateRequest(
        @Schema(description = "변경할 댓글 내용. 미전달하거나 빈 값이면 수정할 내용 없음으로 처리됩니다.", example = "좋아요, 이 일정으로 가요.", nullable = true)
        @Size(max = 300)
        String content
) {
    public JourneyPostCommentUpdateRequest {
        if (content != null) content = content.strip();
    }
}
