package com.dduru.gildongmu.chat.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "채팅 이미지 메시지 정보")
public record ChatMessageImageResponse(
        @Schema(description = "이미지 URL", example = "https://example.com/images/chat-image.png")
        String imageUrl
) {
    public static ChatMessageImageResponse from(String imageUrl) {
        return new ChatMessageImageResponse(imageUrl);
    }
}
