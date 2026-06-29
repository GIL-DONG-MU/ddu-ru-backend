package com.dduru.gildongmu.journey.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record JourneyPostCreateRequest(
        @Schema(description = "여정 게시글 내용. 앞뒤 공백은 제거되며 최대 300자입니다.", example = "오늘 일정 공유합니다.")
        @NotBlank
        @Size(max = 300)
        String content,

        @Schema(description = "게시글 이미지 URL 목록. 최대 4개까지 등록할 수 있습니다.", example = "[\"https://cdn.example.com/journey-posts/1.jpg\"]", nullable = true)
        @Size(max = 4)
        List<String> imageUrls
) {
    public JourneyPostCreateRequest {
        if (content != null) content = content.strip();
    }
}
