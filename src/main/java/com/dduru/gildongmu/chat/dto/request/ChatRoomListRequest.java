package com.dduru.gildongmu.chat.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record ChatRoomListRequest(
        ChatRoomListType roomType,

        @Min(value = 1, message = "size는 1 이상 50 이하로 입력해야 합니다.")
        @Max(value = 50, message = "size는 1 이상 50 이하로 입력해야 합니다.")
        @Schema(description = "페이지 크기. 생략 시 20, 최대 50", example = "20")
        Integer size,

        @Schema(
                description = "다음 페이지 조회 커서. " +
                        "이전 응답의 page.nextCursor 값을 그대로 전달합니다. 첫 페이지 조회 시에는 생략합니다.",
                example = "eyJhY3Rp...MH0="
        )
        String cursor
) {
    public static final int DEFAULT_SIZE = 20;

    public ChatRoomListRequest {
        if (cursor != null) {
            cursor = cursor.isBlank() ? null : cursor.strip();
        }
    }

    public ChatRoomListType roomTypeOrDefault() {
        return roomType == null ? ChatRoomListType.ALL : roomType;
    }

    public int sizeOrDefault() {
        return size == null ? DEFAULT_SIZE : size;
    }
}
