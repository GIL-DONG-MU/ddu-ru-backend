package com.dduru.gildongmu.chat.validation;

import com.dduru.gildongmu.chat.constants.ChatMessageConstants;
import com.dduru.gildongmu.chat.exception.InvalidChatTextException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ChatTextValidator {

    public static String validateAndNormalize(String raw) {
        if (raw == null) {
            throw InvalidChatTextException.missing();
        }

        String text = raw.trim();
        if (text.isEmpty()) {
            throw InvalidChatTextException.blank();
        }
        if (text.length() > ChatMessageConstants.MAX_TEXT_LENGTH) {
            throw InvalidChatTextException.tooLong();
        }
        return text;
    }
}
