package com.dduru.gildongmu.chat.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record GroupChatRoomCreateRequest(
        @NotNull(message = "게시글 ID는 필수입니다.")
        Long postId,

        @NotEmpty
        List<Long> memberUserIds
) {
}
