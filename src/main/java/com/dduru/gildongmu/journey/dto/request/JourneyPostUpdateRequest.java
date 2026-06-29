package com.dduru.gildongmu.journey.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

import java.util.List;

public record JourneyPostUpdateRequest(
        @Schema(description = "변경할 게시글 내용. 미전달 시 기존 값을 유지합니다.", example = "오늘 일정 수정 공유합니다.", nullable = true)
        @Size(max = 300)
        String content,

        @Schema(description = "변경할 이미지 URL 목록. 전달하면 전체 교체되며 최대 4개까지 가능합니다.", example = "[\"https://cdn.example.com/journey-posts/1.jpg\"]", nullable = true)
        @Size(max = 4)
        List<String> imageUrls
) {
    public JourneyPostUpdateRequest {
        if (content != null) content = content.strip();
    }
}
