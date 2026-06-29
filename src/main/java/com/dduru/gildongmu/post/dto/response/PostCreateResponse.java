package com.dduru.gildongmu.post.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record PostCreateResponse(
        @Schema(description = "생성된 게시글 ID", example = "101")
        Long id
) {
}
