package com.dduru.gildongmu.recommendation.support;

import com.dduru.gildongmu.recommendation.dto.result.RecommendationReason;
import com.dduru.gildongmu.survey.domain.TravelTendency;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class RecommendationScoreCalculator {

    private static final double MAX_AXIS_DIFF = TravelTendency.scoreRange();
    private static final double REASON_DIFF_THRESHOLD = 2.0;
    private static final double CAUTION_DIFF_THRESHOLD = 4.0;
    private static final int MAX_REASON_COUNT = 2;
    private static final int MAX_CAUTION_COUNT = 2;

    public RecommendationScore calculate(TravelTendencyScores applicant, TravelTendencyScores host) {
        List<AxisDiff> diffs = Arrays.stream(RecommendationScoreAxis.values())
                .map(axis -> AxisDiff.of(axis, applicant, host))
                .toList();

        double weightedSimilarity = diffs.stream()
                .mapToDouble(diff -> diff.similarity() * diff.axis().weight())
                .sum();
        double clampedSimilarity = clamp(weightedSimilarity);
        int matchPercentage = toPercentage(clampedSimilarity);

        return new RecommendationScore(
                matchPercentage,
                matchReasons(diffs),
                cautionPoints(diffs)
        );
    }

    private double clamp(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    private int toPercentage(double similarity) {
        return (int) Math.round(similarity * 100);
    }

    private List<RecommendationReason> matchReasons(List<AxisDiff> diffs) {
        List<RecommendationReason> reasons = diffs.stream()
                .filter(diff -> diff.diff() <= REASON_DIFF_THRESHOLD)
                .sorted(Comparator.comparingDouble(AxisDiff::diff)
                        .thenComparing(Comparator.comparingDouble((AxisDiff diff) -> diff.axis().weight()).reversed()))
                .limit(MAX_REASON_COUNT)
                .map(diff -> new RecommendationReason(diff.axis().reasonCode(), diff.axis().reasonMessage()))
                .toList();

        if (!reasons.isEmpty()) {
            return reasons;
        }

        return diffs.stream()
                .min(Comparator.comparingDouble(AxisDiff::diff)
                        .thenComparing(Comparator.comparingDouble((AxisDiff diff) -> diff.axis().weight()).reversed()))
                .map(diff -> List.of(new RecommendationReason(diff.axis().reasonCode(), diff.axis().reasonMessage())))
                .orElseGet(List::of);
    }

    private List<RecommendationReason> cautionPoints(List<AxisDiff> diffs) {
        return diffs.stream()
                .filter(diff -> diff.diff() >= CAUTION_DIFF_THRESHOLD)
                .sorted(Comparator.comparingDouble(AxisDiff::diff).reversed()
                        .thenComparing(Comparator.comparingDouble((AxisDiff diff) -> diff.axis().weight()).reversed()))
                .limit(MAX_CAUTION_COUNT)
                .map(diff -> new RecommendationReason(diff.axis().cautionCode(), diff.axis().cautionMessage()))
                .toList();
    }

    private record AxisDiff(
            RecommendationScoreAxis axis,
            double diff
    ) {
        private static AxisDiff of(RecommendationScoreAxis axis, TravelTendencyScores applicant, TravelTendencyScores host) {
            return new AxisDiff(axis, Math.abs(axis.scoreOf(applicant) - axis.scoreOf(host)));
        }

        private double similarity() {
            return 1 - (diff / MAX_AXIS_DIFF);
        }
    }
}
