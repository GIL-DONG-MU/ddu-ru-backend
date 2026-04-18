package com.dduru.gildongmu.chat.validation;

import com.dduru.gildongmu.chat.constants.ChatMessageConstants;
import com.dduru.gildongmu.chat.exception.InvalidChatImageUrlException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.net.URI;
import java.net.URISyntaxException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ChatImageUrlValidator {

    /**
     * 채팅 이미지 URL 검증: 비어 있지 않고, 길이 제한 내이며, 절대 URI이고 스킴은 https 만 허용한다.
     */
    public static String validateAndNormalize(String raw) {
        if (raw == null) {
            throw InvalidChatImageUrlException.missing();
        }
        String url = raw.trim();
        if (url.isEmpty()) {
            throw InvalidChatImageUrlException.blank();
        }
        if (url.length() > ChatMessageConstants.MAX_IMAGE_URL_LENGTH) {
            throw InvalidChatImageUrlException.tooLong();
        }
        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            throw InvalidChatImageUrlException.invalidFormat();
        }
        if (!uri.isAbsolute()) {
            throw InvalidChatImageUrlException.notAbsolute();
        }
        if (uri.getScheme() == null || !"https".equalsIgnoreCase(uri.getScheme())) {
            throw InvalidChatImageUrlException.nonHttpsScheme();
        }
        return url;
    }
}
