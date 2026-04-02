package com.dduru.gildongmu.survey.converter;

import com.dduru.gildongmu.survey.domain.enums.*;

import java.util.List;

public record ParsedSurveyData(
        RhythmQuestion1 rhythmQ1,
        RhythmQuestion2 rhythmQ2,
        RhythmQuestion3 rhythmQ3,
        ConsumptionQuestion1 consumptionQ1,
        ConsumptionQuestion2 consumptionQ2,
        ConsumptionQuestion3 consumptionQ3,
        EnergyQuestion1 energyQ1,
        EnergyQuestion2 energyQ2,
        EnergyQuestion3 energyQ3,
        DecisionQuestion1 decisionQ1,
        DecisionQuestion2 decisionQ2,
        DecisionQuestion3 decisionQ3,
        RecordStyleQuestion recordStyle,
        List<ActivityTag> activityTags
) {
}
