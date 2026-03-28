package com.dduru.gildongmu.tag.service;

import com.dduru.gildongmu.post.exception.InvalidTagsException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("TagValidator")
class TagValidatorTest {

    @Test
    void nullOrEmpty_ok() {
        assertThatCode(() -> TagValidator.validateOrThrow(null)).doesNotThrowAnyException();
        assertThatCode(() -> TagValidator.validateOrThrow(List.of())).doesNotThrowAnyException();
    }

    @Test
    void moreThanFourTags_throws() {
        List<String> five = IntStream.range(0, 5).mapToObj(i -> "a" + i).toList();
        assertThatThrownBy(() -> TagValidator.validateOrThrow(five))
                .isInstanceOf(InvalidTagsException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"가나다라", "제주2", "ABCDE", "abcdefg", "가ab", "1234567", "가나다라a1"})
    @DisplayName("허용되는 단일 태그")
    void validSingleTag_ok(String tag) {
        assertThatCode(() -> TagValidator.validateOrThrow(List.of(tag))).doesNotThrowAnyException();
    }

    @ParameterizedTest
    @ValueSource(strings = {"가나다라마", "ABCDEFGH", "서울12345678", "", "  ", "my tag"})
    @DisplayName("거부되는 단일 태그")
    void invalidSingleTag_throws(String tag) {
        assertThatThrownBy(() -> TagValidator.validateOrThrow(List.of(tag)))
                .isInstanceOf(InvalidTagsException.class);
    }

    @Test
    void trimsLeadingAndTrailingWhitespace_ok() {
        assertThatCode(() -> TagValidator.validateOrThrow(List.of(" 태그 "))).doesNotThrowAnyException();
    }

    @Test
    void specialCharacter_throws() {
        assertThatThrownBy(() -> TagValidator.validateOrThrow(List.of("서울!")))
                .isInstanceOf(InvalidTagsException.class);
    }
}
