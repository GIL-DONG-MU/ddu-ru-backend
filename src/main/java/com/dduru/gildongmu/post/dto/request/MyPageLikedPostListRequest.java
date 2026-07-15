package com.dduru.gildongmu.post.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record MyPageLikedPostListRequest(
        @Schema(description = "다음 페이지 조회용 커서 (PostLike ID). 첫 페이지에서는 생략합니다.", example = "120", nullable = true)
        Long cursor,
        @Schema(description = "페이지 크기. 생략하거나 1 미만 또는 50 초과이면 10으로 보정됩니다.", example = "10")
        Integer size
) {
    public MyPageLikedPostListRequest {
        if (size == null || size <= 0 || size > 50) size = 10;
    }
}
