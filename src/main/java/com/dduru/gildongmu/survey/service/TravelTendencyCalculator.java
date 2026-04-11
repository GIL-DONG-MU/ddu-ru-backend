package com.dduru.gildongmu.survey.service;

import com.dduru.gildongmu.survey.domain.Survey;
import com.dduru.gildongmu.survey.domain.enums.AxisBinaryScore;
import com.dduru.gildongmu.survey.dto.response.TendencyScoreResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class TravelTendencyCalculator {

    private static final int MAIN_AXIS_DENOMINATOR = Survey.mainAxisMaxRaw();

    public TendencyScoreResponse calculate(Survey survey) {
        double rhythmScore = calculateRhythmScore(survey);
        double energyScore = calculateEnergyScore(survey);
        double consumptionScore = calculateConsumptionScore(survey);
        double decisionScore = calculateDecisionScore(survey);

        return new TendencyScoreResponse(
                roundToOneDecimal(rhythmScore),
                roundToOneDecimal(energyScore),
                roundToOneDecimal(consumptionScore),
                roundToOneDecimal(decisionScore)
        );
    }

    private double calculateRhythmScore(Survey survey) {
        return scaleTo10(sumScores(survey.getRhythmQ1(), survey.getRhythmQ2(), survey.getRhythmQ3()));
    }

    private double calculateEnergyScore(Survey survey) {
        return scaleTo10(sumScores(survey.getEnergyQ1(), survey.getEnergyQ2(), survey.getEnergyQ3()));
    }

    private double calculateConsumptionScore(Survey survey) {
        return scaleTo10(sumScores(survey.getConsumptionQ1(), survey.getConsumptionQ2(), survey.getConsumptionQ3()));
    }

    private double calculateDecisionScore(Survey survey) {
        return scaleTo10(sumScores(survey.getDecisionQ1(), survey.getDecisionQ2(), survey.getDecisionQ3()));
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

    private static double roundToOneDecimal(double value) {
        return BigDecimal.valueOf(value).setScale(1, RoundingMode.HALF_UP).doubleValue();
    }
}
