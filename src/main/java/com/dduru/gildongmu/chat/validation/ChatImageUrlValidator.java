package com.dduru.gildongmu.chat.validation;

import com.dduru.gildongmu.chat.constants.ChatMessageConstants;
import com.dduru.gildongmu.chat.exception.InvalidChatImageUrlException;
import com.dduru.gildongmu.common.config.S3Properties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class ChatImageUrlValidator {

    private static final String CHATS_PATH_PREFIX = "/chats/";
    private static final List<String> ALLOWED_EXTENSIONS = List.of(".jpg", ".jpeg", ".png", ".gif");

    private final S3Properties s3Properties;

    /**
     * 채팅 이미지 URL 검증: 비어 있지 않고, 길이 제한 내이며, 앱이 발급한 chats 경로의 https URL만 허용한다.
     */
    public String validateAndNormalize(String raw) {
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
        if (!isAllowedChatImageUri(uri)) {
            throw InvalidChatImageUrlException.notAllowed();
        }
        return url;
    }

    private boolean isAllowedChatImageUri(URI uri) {
        if (uri.getRawQuery() != null || uri.getRawFragment() != null) {
            return false;
        }
        if (uri.getHost() == null || !expectedHost().equalsIgnoreCase(uri.getHost())) {
            return false;
        }

        String path = uri.getPath();
        if (path == null || !path.startsWith(CHATS_PATH_PREFIX) || path.length() == CHATS_PATH_PREFIX.length()) {
            return false;
        }

        String lowerPath = path.toLowerCase(Locale.ROOT);
        return ALLOWED_EXTENSIONS.stream().anyMatch(lowerPath::endsWith);
    }

    private String expectedHost() {
        return "%s.s3.%s.amazonaws.com".formatted(s3Properties.getBucket(), s3Properties.getRegion());
    }
}
