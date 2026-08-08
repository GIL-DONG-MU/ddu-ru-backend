package com.dduru.gildongmu.recommendation.domain;

import com.dduru.gildongmu.common.exception.ErrorCode;
import com.dduru.gildongmu.recommendation.exception.InvalidMateRecommendationMatchPercentageException;
import com.dduru.gildongmu.recommendation.exception.InvalidMateRecommendationRankException;
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
                .isInstanceOfSatisfying(InvalidMateRecommendationRankException.class, exception -> {
                    assertThat(exception.getErrorCode())
                            .isEqualTo(ErrorCode.INVALID_MATE_RECOMMENDATION_RANK);
                    assertThat(exception).hasMessage(
                            "추천 순위는 %d 이상 %d 이하여야 합니다.".formatted(
                                    RecommendationPolicy.MIN_RECOMMENDATION_RANK,
                                    RecommendationPolicy.MAX_DAILY_RECOMMENDATIONS
                            )
                    );
                });
    }

    @Test
    @DisplayName("추천 일치율이 정책 범위를 벗어나면 비즈니스 예외를 발생시킨다")
    void rejectsMatchPercentageOutsidePolicyRange() {
        int invalidPercentage = RecommendationPolicy.MAX_MATCH_PERCENTAGE + 1;

        assertThatThrownBy(() -> create(1, invalidPercentage))
                .isInstanceOfSatisfying(InvalidMateRecommendationMatchPercentageException.class, exception -> {
                    assertThat(exception.getErrorCode())
                            .isEqualTo(ErrorCode.INVALID_MATE_RECOMMENDATION_MATCH_PERCENTAGE);
                    assertThat(exception).hasMessage(
                            "추천 일치율은 %d 이상 %d 이하여야 합니다.".formatted(
                                    RecommendationPolicy.MIN_MATCH_PERCENTAGE,
                                    RecommendationPolicy.MAX_MATCH_PERCENTAGE
                            )
                    );
                });
    }

    private MateRecommendation create(int recommendationRank) {
        return create(recommendationRank, 90);
    }

    private MateRecommendation create(int recommendationRank, int matchPercentage) {
        return MateRecommendation.create(
                null,
                null,
                recommendationRank,
                matchPercentage,
                "[]",
                "[]"
        );
    }
}
