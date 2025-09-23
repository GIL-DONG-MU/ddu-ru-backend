package com.dduru.gildongmu.post.dto;

import jakarta.validation.constraints.NotNull;

public record PostStatusUpdateRequest(
    @NotNull(message = "모집 상태는 필수입니다.")
    boolean open
) {
}
