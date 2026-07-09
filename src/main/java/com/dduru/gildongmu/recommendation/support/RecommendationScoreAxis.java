package com.dduru.gildongmu.recommendation.support;

import java.util.Arrays;
import java.util.function.ToDoubleFunction;

public enum RecommendationScoreAxis {
    RHYTHM(
            0.30,
            TravelTendencyScores::rhythmScore,
            "RHYTHM_MATCH",
            "여행 리듬이 잘 맞아요",
            "RHYTHM_DIFFERENCE",
            "기상과 이동 리듬은 미리 맞춰보세요"
    ),
    ENERGY(
            0.30,
            TravelTendencyScores::energyScore,
            "ENERGY_MATCH",
            "활동 에너지가 비슷해요",
            "ENERGY_DIFFERENCE",
            "활동량 기대치는 미리 확인해보세요"
    ),
    CONSUMPTION(
            0.30,
            TravelTendencyScores::consumptionScore,
            "CONSUMPTION_MATCH",
            "여행 소비 성향이 비슷해요",
            "CONSUMPTION_DIFFERENCE",
            "예산 사용 방식은 미리 이야기해보세요"
    ),
    DECISION(
            0.10,
            TravelTendencyScores::decisionScore,
            "DECISION_MATCH",
            "의사결정 방식이 잘 맞아요",
            "DECISION_DIFFERENCE",
            "일정 결정 방식은 미리 조율해보세요"
    );

    private static final double TOTAL_WEIGHT = 1.0;
    private static final double WEIGHT_EPSILON = 0.000_001;

    static {
        double totalWeight = Arrays.stream(values())
                .mapToDouble(RecommendationScoreAxis::weight)
                .sum();
        if (Math.abs(totalWeight - TOTAL_WEIGHT) > WEIGHT_EPSILON) {
            throw new IllegalStateException(
                    "Invalid recommendation score axis weights: expectedTotalWeight=%s, actualTotalWeight=%s, weights=%s"
                            .formatted(TOTAL_WEIGHT, totalWeight, weightSummary())
            );
        }
    }

    private final double weight;
    private final ToDoubleFunction<TravelTendencyScores> scoreSelector;
    private final String reasonCode;
    private final String reasonMessage;
    private final String cautionCode;
    private final String cautionMessage;

    RecommendationScoreAxis(
            double weight,
            ToDoubleFunction<TravelTendencyScores> scoreSelector,
            String reasonCode,
            String reasonMessage,
            String cautionCode,
            String cautionMessage
    ) {
        this.weight = weight;
        this.scoreSelector = scoreSelector;
        this.reasonCode = reasonCode;
        this.reasonMessage = reasonMessage;
        this.cautionCode = cautionCode;
        this.cautionMessage = cautionMessage;
    }

    double weight() {
        return weight;
    }

    double scoreOf(TravelTendencyScores scores) {
        return scoreSelector.applyAsDouble(scores);
    }

    String reasonCode() {
        return reasonCode;
    }

    String reasonMessage() {
        return reasonMessage;
    }

    String cautionCode() {
        return cautionCode;
    }

    String cautionMessage() {
        return cautionMessage;
    }

    private static String weightSummary() {
        return Arrays.stream(values())
                .map(axis -> axis.name() + "=" + axis.weight)
                .toList()
                .toString();
    }
}
