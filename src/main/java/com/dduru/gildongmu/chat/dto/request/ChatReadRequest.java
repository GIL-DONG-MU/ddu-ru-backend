package com.dduru.gildongmu.chat.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ChatReadRequest(
        @NotNull(message = "lastReadMessageId는 필수입니다.")
        @Positive(message = "lastReadMessageId는 1 이상이어야 합니다.")
        Long lastReadMessageId
) {
}
