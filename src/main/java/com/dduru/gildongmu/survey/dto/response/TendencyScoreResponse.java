package com.dduru.gildongmu.survey.dto.response;

public record TendencyScoreResponse(
        double rhythmScore,
        double energyScore,
        double consumptionScore,
        double decisionScore
) {
}
