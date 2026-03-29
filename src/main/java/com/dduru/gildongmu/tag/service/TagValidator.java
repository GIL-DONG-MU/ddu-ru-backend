package com.dduru.gildongmu.tag.service;

import com.dduru.gildongmu.post.exception.InvalidTagsException;

import java.util.List;

public final class TagValidator {

    public static final int MAX_TAG_COUNT = 4;
    private static final int MAX_HANGUL_SYLLABLES = 4;
    private static final int MAX_NON_HANGUL_CHARS = 7;

    private TagValidator() {
    }

    public static void validateOrThrow(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return;
        }
        if (tags.size() > MAX_TAG_COUNT) {
            throw InvalidTagsException.tooMany();
        }
        for (String raw : tags) {
            if (raw == null) {
                throw InvalidTagsException.blankOrWhitespace();
            }
            String tag = raw.strip();
            if (tag.isEmpty()) {
                throw InvalidTagsException.blankOrWhitespace();
            }
            if (containsWhitespace(tag)) {
                throw InvalidTagsException.blankOrWhitespace();
            }
            if (!isValidTag(tag)) {
                throw InvalidTagsException.invalidLengthOrCharacters();
            }
        }
    }

    private static boolean containsWhitespace(String s) {
        return s.codePoints().anyMatch(Character::isWhitespace);
    }

    private static boolean isHangulSyllable(int cp) {
        return cp >= 0xAC00 && cp <= 0xD7A3;
    }

    private static boolean isAsciiLetterOrDigit(int cp) {
        return (cp >= 'A' && cp <= 'Z') || (cp >= 'a' && cp <= 'z') || (cp >= '0' && cp <= '9');
    }

    private static boolean isValidTag(String tag) {
        int hangul = 0;
        int nonHangul = 0;
        for (int i = 0; i < tag.length(); ) {
            int cp = tag.codePointAt(i);
            i += Character.charCount(cp);
            if (isHangulSyllable(cp)) {
                hangul++;
            } else {
                if (!isAsciiLetterOrDigit(cp)) {
                    return false;
                }
                nonHangul++;
            }
        }
        if (hangul > 0) {
            return hangul <= MAX_HANGUL_SYLLABLES && nonHangul <= MAX_NON_HANGUL_CHARS;
        }
        return nonHangul >= 1 && nonHangul <= MAX_NON_HANGUL_CHARS;
    }
}
