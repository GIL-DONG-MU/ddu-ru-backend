package com.dduru.gildongmu.recommendation.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("MateRecommendation 테스트")
class MateRecommendationTest {

    @Test
    @DisplayName("추천 순위는 일일 추천 정책의 최댓값까지 허용한다")
    void allowsMaximumDailyRecommendationRank() {
        MateRecommendation recommendation = create(RecommendationPolicy.MAX_DAILY_RECOMMENDATIONS);

        assertThat(recommendation.getRecommendationRank())
                .isEqualTo(RecommendationPolicy.MAX_DAILY_RECOMMENDATIONS);
    }

    @Test
    @DisplayName("추천 순위가 정책 최댓값을 넘으면 정책 값이 포함된 예외를 발생시킨다")
    void rejectsRankAboveMaximumDailyRecommendations() {
        int invalidRank = RecommendationPolicy.MAX_DAILY_RECOMMENDATIONS + 1;

        assertThatThrownBy(() -> create(invalidRank))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "Recommendation rank must be between 1 and "
                                + RecommendationPolicy.MAX_DAILY_RECOMMENDATIONS
                );
    }

    private MateRecommendation create(int recommendationRank) {
        return MateRecommendation.create(
                null,
                null,
                recommendationRank,
                90,
                "[]",
                "[]"
        );
    }
}
