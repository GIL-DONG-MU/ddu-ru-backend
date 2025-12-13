package com.dduru.gildongmu.nickname.generator;

import com.dduru.gildongmu.nickname.enums.NicknameTheme;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AdjectiveProvider 테스트")
class AdjectiveProviderTest {

    private AdjectiveProvider adjectiveProvider;

    @BeforeEach
    void setUp() {
        adjectiveProvider = new AdjectiveProvider();
    }

    @ParameterizedTest
    @EnumSource(value = NicknameTheme.class, names = {"TRAVEL", "NATURE", "ANIMAL", "FOOD", "SPACE", "FANTASY"})
    @DisplayName("각 테마별로 형용사를 제공한다")
    void getRandomAdjective_withEachTheme_returnsAdjective(NicknameTheme theme) {
        // when
        String adjective = adjectiveProvider.getRandomAdjective(theme);

        // then
        assertThat(adjective).isNotBlank();
    }

    @Test
    @DisplayName("RANDOM 테마는 모든 테마에서 형용사를 선택한다")
    void getRandomAdjective_withRandomTheme_returnsAdjectiveFromAnyTheme() {
        // when
        String adjective = adjectiveProvider.getRandomAdjective(NicknameTheme.RANDOM);

        // then
        assertThat(adjective).isNotBlank();
    }

    @ParameterizedTest
    @EnumSource(value = NicknameTheme.class, names = {"TRAVEL", "NATURE", "ANIMAL", "FOOD", "SPACE", "FANTASY"})
    @DisplayName("각 테마별 형용사 목록을 반환한다")
    void getAdjectives_withEachTheme_returnsAdjectiveList(NicknameTheme theme) {
        // when
        List<String> adjectives = adjectiveProvider.getAdjectives(theme);

        // then
        assertThat(adjectives).isNotEmpty();
        assertThat(adjectives).allMatch(adj -> adj != null && !adj.isBlank());
    }

    @Test
    @DisplayName("RANDOM 테마는 모든 형용사를 반환한다")
    void getAdjectives_withRandomTheme_returnsAllAdjectives() {
        // when
        List<String> allAdjectives = adjectiveProvider.getAdjectives(NicknameTheme.RANDOM);

        // then
        int expectedMinCount = 6 * 10; // 최소 6개 테마 × 10개 형용사
        assertThat(allAdjectives.size()).isGreaterThanOrEqualTo(expectedMinCount);
    }

    @Test
    @DisplayName("랜덤 형용사는 매번 달라질 수 있다")
    void getRandomAdjective_multipleInvocations_mayReturnDifferentValues() {
        // given
        NicknameTheme theme = NicknameTheme.TRAVEL;
        
        // when - 여러 번 호출
        java.util.Set<String> results = new java.util.HashSet<>();
        for (int i = 0; i < 100; i++) {
            results.add(adjectiveProvider.getRandomAdjective(theme));
        }

        // then - 여러 다른 형용사가 반환되어야 함
        assertThat(results.size()).isGreaterThan(1);
    }
}
