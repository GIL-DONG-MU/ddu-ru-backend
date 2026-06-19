package com.dduru.gildongmu.chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "채팅 메시지 페이지 정보")
public record ChatMessagePageResponse(
        @Schema(description = "요청에 적용된 페이지 크기", example = "20")
        int size,

        @Schema(description = "더 과거 메시지 존재 여부", example = "true")
        boolean hasNext,

        @Schema(description = "다음 과거 페이지 조회 커서. 다음 요청의 beforeMessageId로 전달하며 hasNext=false이면 null", example = "100", nullable = true)
        Long nextCursor
) {
}
