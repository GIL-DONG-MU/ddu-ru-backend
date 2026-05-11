package com.dduru.gildongmu.chat.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ChatMessageConstants {

    public static final int MAX_TEXT_LENGTH = 4_000;
    public static final int MAX_IMAGE_URL_LENGTH = 2_048;
    public static final String UNKNOWN_NICKNAME = "알 수 없는 사용자";
}
