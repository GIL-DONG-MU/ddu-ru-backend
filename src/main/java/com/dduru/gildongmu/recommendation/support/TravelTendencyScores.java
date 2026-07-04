package com.dduru.gildongmu.recommendation.support;

import com.dduru.gildongmu.recommendation.dto.query.RecommendablePostQueryResult;
import com.dduru.gildongmu.survey.domain.TravelTendency;

import java.math.BigDecimal;

public record TravelTendencyScores(
        double rhythmScore,
        double energyScore,
        double consumptionScore,
        double decisionScore
) {
    public static TravelTendencyScores from(TravelTendency tendency) {
        return new TravelTendencyScores(
                toDouble(tendency.getRhythmScore()),
                toDouble(tendency.getEnergyScore()),
                toDouble(tendency.getConsumptionScore()),
                toDouble(tendency.getDecisionScore())
        );
    }

    public static TravelTendencyScores from(RecommendablePostQueryResult post) {
        return new TravelTendencyScores(
                toDouble(post.hostRhythmScore()),
                toDouble(post.hostEnergyScore()),
                toDouble(post.hostConsumptionScore()),
                toDouble(post.hostDecisionScore())
        );
    }

    private static double toDouble(BigDecimal value) {
        return value.doubleValue();
    }
}
