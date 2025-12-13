package com.dduru.gildongmu.nickname.generator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("NounProvider 테스트")
class NounProviderTest {

    private NounProvider nounProvider;

    @BeforeEach
    void setUp() {
        nounProvider = new NounProvider();
    }

    @Test
    @DisplayName("랜덤 명사를 반환한다")
    void getRandomNoun_returnsNoun() {
        // when
        String noun = nounProvider.getRandomNoun();

        // then
        assertThat(noun).isNotBlank();
    }

    @Test
    @DisplayName("여러 번 호출 시 다양한 명사를 반환한다")
    void getRandomNoun_multipleInvocations_returnsDifferentValues() {
        // when
        Set<String> results = new HashSet<>();
        for (int i = 0; i < 100; i++) {
            results.add(nounProvider.getRandomNoun());
        }

        // then
        assertThat(results.size()).isGreaterThan(1);
    }
}
