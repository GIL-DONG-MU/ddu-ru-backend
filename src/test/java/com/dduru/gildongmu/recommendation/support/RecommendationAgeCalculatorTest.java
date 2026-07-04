package com.dduru.gildongmu.recommendation.support;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RecommendationAgeCalculator 테스트")
class RecommendationAgeCalculatorTest {

    @Test
    @DisplayName("KST 기준 오늘 생일이 지났는지에 따라 만 나이를 계산한다")
    void calculateAge() {
        LocalDate today = LocalDate.of(2026, 7, 4);

        assertThat(RecommendationAgeCalculator.calculate(LocalDate.of(2000, 7, 4), today)).isEqualTo(26);
        assertThat(RecommendationAgeCalculator.calculate(LocalDate.of(2000, 7, 5), today)).isEqualTo(25);
    }

    @Test
    @DisplayName("생년월일이 없으면 null을 반환한다")
    void returnsNullWhenBirthdayMissing() {
        assertThat(RecommendationAgeCalculator.calculate(null, LocalDate.of(2026, 7, 4))).isNull();
    }
}
