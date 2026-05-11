package com.dduru.gildongmu.chat.dto.response;

public record ChatMessageImageResponse(
        String imageUrl
) {
    public static ChatMessageImageResponse from(String imageUrl) {
        return new ChatMessageImageResponse(imageUrl);
    }
}
