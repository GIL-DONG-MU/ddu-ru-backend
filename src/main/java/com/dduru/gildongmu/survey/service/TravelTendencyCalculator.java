package com.dduru.gildongmu.survey.service;

import com.dduru.gildongmu.survey.domain.Survey;
import com.dduru.gildongmu.survey.domain.enums.AxisBinaryScore;
import com.dduru.gildongmu.survey.dto.response.TendencyScoreResponse;
import org.springframework.stereotype.Component;

@Component
public class TravelTendencyCalculator {

    private static final int MAIN_AXIS_DENOMINATOR = Survey.mainAxisMaxRaw();

    public TendencyScoreResponse calculate(Survey survey) {
        double rhythmScore = scaleTo10(sumScores(
                survey.getRhythmQ1(), survey.getRhythmQ2(), survey.getRhythmQ3()));
        double consumptionScore = scaleTo10(sumScores(
                survey.getConsumptionQ1(), survey.getConsumptionQ2(), survey.getConsumptionQ3()));
        double decisionScore = scaleTo10(sumScores(
                survey.getDecisionQ1(), survey.getDecisionQ2(), survey.getDecisionQ3()));
        double energyScore = scaleTo10(sumScores(
                survey.getEnergyQ1(), survey.getEnergyQ2(), survey.getEnergyQ3()));

        return new TendencyScoreResponse(rhythmScore, energyScore, consumptionScore, decisionScore);
    }

    private static int sumScores(AxisBinaryScore... parts) {
        int sum = 0;
        for (AxisBinaryScore part : parts) {
            sum += part.getAxisScorePoints();
        }
        return sum;
    }

    private static double scaleTo10(int rawSum) {
        return (rawSum / (double) MAIN_AXIS_DENOMINATOR) * 10.0;
    }
}
