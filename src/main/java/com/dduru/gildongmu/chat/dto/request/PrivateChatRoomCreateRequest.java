package com.dduru.gildongmu.chat.dto.request;

import jakarta.validation.constraints.NotNull;

public record PrivateChatRoomCreateRequest(
        @NotNull(message = "게시글 ID는 필수입니다.")
        Long postId
) {
}
