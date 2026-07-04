package com.dduru.gildongmu.recommendation.support;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RecommendationScoreCalculator 테스트")
class RecommendationScoreCalculatorTest {

    private final RecommendationScoreCalculator calculator = new RecommendationScoreCalculator();

    @Test
    @DisplayName("성향 점수가 모두 같으면 적합도 100점과 상위 reason 2개를 반환한다")
    void calculatePerfectMatch() {
        TravelTendencyScores applicant = scores(5, 5, 5, 5);
        TravelTendencyScores host = scores(5, 5, 5, 5);

        RecommendationScore score = calculator.calculate(applicant, host);

        assertThat(score.matchPercentage()).isEqualTo(100);
        assertThat(score.matchReasons())
                .extracting("code")
                .containsExactly("RHYTHM_MATCH", "ENERGY_MATCH");
        assertThat(score.cautionPoints()).isEmpty();
    }

    @Test
    @DisplayName("성향 점수가 모두 반대면 적합도 0점과 fallback reason, caution 2개를 반환한다")
    void calculateOppositeMatch() {
        TravelTendencyScores applicant = scores(0, 0, 0, 0);
        TravelTendencyScores host = scores(10, 10, 10, 10);

        RecommendationScore score = calculator.calculate(applicant, host);

        assertThat(score.matchPercentage()).isEqualTo(0);
        assertThat(score.matchReasons())
                .extracting("code")
                .containsExactly("RHYTHM_MATCH");
        assertThat(score.cautionPoints())
                .extracting("code")
                .containsExactly("RHYTHM_DIFFERENCE", "ENERGY_DIFFERENCE");
    }

    @Test
    @DisplayName("가중합을 0~100 정수로 반올림한다")
    void calculateWeightedRoundedScore() {
        TravelTendencyScores applicant = scores(10, 10, 10, 10);
        TravelTendencyScores host = scores(9, 8, 7, 6);

        RecommendationScore score = calculator.calculate(applicant, host);

        assertThat(score.matchPercentage()).isEqualTo(78);
    }

    private TravelTendencyScores scores(double rhythm, double energy, double consumption, double decision) {
        return new TravelTendencyScores(rhythm, energy, consumption, decision);
    }
}
