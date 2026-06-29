package com.dduru.gildongmu.post.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record PostStatusUpdateRequest(
        @Schema(description = "모집 상태 변경 값. true는 모집중, false는 모집마감입니다.", example = "false")
        @NotNull(message = "모집 상태는 필수입니다.")
        Boolean open
) {
}
