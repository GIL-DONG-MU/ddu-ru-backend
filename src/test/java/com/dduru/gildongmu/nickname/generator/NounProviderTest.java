package com.dduru.gildongmu.nickname.generator;

import com.dduru.gildongmu.nickname.enums.NicknameTheme;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("NounProvider 테스트")
class NounProviderTest {

    private NounProvider nounProvider;

    @BeforeEach
    void setUp() {
        nounProvider = new NounProvider();
    }

    @ParameterizedTest
    @EnumSource(value = NicknameTheme.class, names = {"TRAVEL", "NATURE", "ANIMAL", "FOOD", "SPACE", "FANTASY"})
    @DisplayName("각 테마별로 명사를 제공한다")
    void getRandomNoun_withEachTheme_returnsNoun(NicknameTheme theme) {
        // when
        String noun = nounProvider.getRandomNoun(theme);

        // then
        assertThat(noun).isNotBlank();
    }

    @Test
    @DisplayName("RANDOM 테마는 모든 테마에서 명사를 선택한다")
    void getRandomNoun_withRandomTheme_returnsNounFromAnyTheme() {
        // when
        String noun = nounProvider.getRandomNoun(NicknameTheme.RANDOM);

        // then
        assertThat(noun).isNotBlank();
    }

    @ParameterizedTest
    @EnumSource(value = NicknameTheme.class, names = {"TRAVEL", "NATURE", "ANIMAL", "FOOD", "SPACE", "FANTASY"})
    @DisplayName("각 테마별 명사 목록을 반환한다")
    void getNouns_withEachTheme_returnsNounList(NicknameTheme theme) {
        // when
        List<String> nouns = nounProvider.getNouns(theme);

        // then
        assertThat(nouns).isNotEmpty();
        assertThat(nouns).allMatch(noun -> noun != null && !noun.isBlank());
    }

    @Test
    @DisplayName("RANDOM 테마는 모든 명사를 반환한다")
    void getNouns_withRandomTheme_returnsAllNouns() {
        // when
        List<String> allNouns = nounProvider.getNouns(NicknameTheme.RANDOM);

        // then
        int expectedMinCount = 6 * 15; // 최소 6개 테마 × 15개 명사
        assertThat(allNouns.size()).isGreaterThanOrEqualTo(expectedMinCount);
    }

    @Test
    @DisplayName("랜덤 명사는 매번 달라질 수 있다")
    void getRandomNoun_multipleInvocations_mayReturnDifferentValues() {
        // given
        NicknameTheme theme = NicknameTheme.ANIMAL;
        
        // when - 여러 번 호출
        java.util.Set<String> results = new java.util.HashSet<>();
        for (int i = 0; i < 100; i++) {
            results.add(nounProvider.getRandomNoun(theme));
        }

        // then - 여러 다른 명사가 반환되어야 함
        assertThat(results.size()).isGreaterThan(1);
    }
}
