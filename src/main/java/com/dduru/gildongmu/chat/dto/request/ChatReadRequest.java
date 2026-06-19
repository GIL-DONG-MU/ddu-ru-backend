package com.dduru.gildongmu.chat.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "채팅방 읽음 처리 요청")
public record ChatReadRequest(
        @Schema(description = "현재 사용자가 이 메시지까지 읽었음을 서버에 반영할 메시지 ID", example = "120")
        @NotNull(message = "lastReadMessageId는 필수입니다.")
        @Positive(message = "lastReadMessageId는 1 이상이어야 합니다.")
        Long lastReadMessageId
) {
}
