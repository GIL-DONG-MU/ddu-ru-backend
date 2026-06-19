package com.dduru.gildongmu.chat.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

@Schema(description = "채팅방 메시지 목록 조회 조건")
public record ChatMessageRetrieveRequest(
        @Schema(
                description = "이 메시지 ID보다 오래된 메시지를 조회합니다. 첫 페이지 조회 시에는 생략합니다.",
                example = "120"
        )
        @Positive(message = "beforeMessageId는 1 이상이어야 합니다.")
        Long beforeMessageId,

        @Schema(description = "페이지 크기. 생략 시 20, 최대 50", example = "20")
        @Min(value = 1, message = "size는 1 이상 50 이하로 입력해야 합니다.")
        @Max(value = 50, message = "size는 1 이상 50 이하로 입력해야 합니다.")
        Integer size
) {
    public static final int DEFAULT_SIZE = 20;
    public static final int MAX_SIZE = 50;

    public int sizeOrDefault() {
        return size == null ? DEFAULT_SIZE : size;
    }
}
