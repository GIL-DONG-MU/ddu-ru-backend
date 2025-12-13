package com.dduru.gildongmu.nickname.generator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AdjectiveProvider 테스트")
class AdjectiveProviderTest {

    private AdjectiveProvider adjectiveProvider;

    @BeforeEach
    void setUp() {
        adjectiveProvider = new AdjectiveProvider();
    }

    @Test
    @DisplayName("랜덤 형용사를 반환한다")
    void getRandomAdjective_returnsAdjective() {
        // when
        String adjective = adjectiveProvider.getRandomAdjective();

        // then
        assertThat(adjective).isNotBlank();
    }

    @Test
    @DisplayName("여러 번 호출 시 다양한 형용사를 반환한다")
    void getRandomAdjective_multipleInvocations_returnsDifferentValues() {
        // when
        Set<String> results = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            results.add(adjectiveProvider.getRandomAdjective());
        }

        // then
        assertThat(results.size()).isGreaterThan(1);
    }
}
