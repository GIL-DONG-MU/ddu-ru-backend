package com.dduru.gildongmu.survey.converter;

import com.dduru.gildongmu.common.enums.CodedEnum;
import com.dduru.gildongmu.common.enums.EnumUtils;
import com.dduru.gildongmu.survey.domain.Survey;
import com.dduru.gildongmu.survey.domain.enums.*;
import com.dduru.gildongmu.survey.dto.request.SurveyRequest;
import com.dduru.gildongmu.survey.exception.InvalidSurveyAnswerCodeException;
import com.dduru.gildongmu.user.domain.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SurveyConverter {
    public Survey toEntity(User user, ParsedSurveyData parsed) {
        return Survey.createSurvey(user,
                parsed.rhythmQ1(), parsed.rhythmQ2(), parsed.rhythmQ3(),
                parsed.consumptionQ1(), parsed.consumptionQ2(), parsed.consumptionQ3(),
                parsed.energyQ1(), parsed.energyQ2(), parsed.energyQ3(),
                parsed.decisionQ1(), parsed.decisionQ2(), parsed.decisionQ3(),
                parsed.recordStyle(),
                parsed.activityTags());
    }

    public ParsedSurveyData parseRequest(SurveyRequest request) {
        RhythmQuestion1 rhythmQ1 = toEnum(RhythmQuestion1.class, request.rhythmQ1());
        RhythmQuestion2 rhythmQ2 = toEnum(RhythmQuestion2.class, request.rhythmQ2());
        RhythmQuestion3 rhythmQ3 = toEnum(RhythmQuestion3.class, request.rhythmQ3());
        List<ActivityTag> activityTags = toEnumList(ActivityTag.class, request.activityTags());
        ConsumptionQuestion1 consumptionQ1 = toEnum(ConsumptionQuestion1.class, request.consumptionQ1());
        ConsumptionQuestion2 consumptionQ2 = toEnum(ConsumptionQuestion2.class, request.consumptionQ2());
        ConsumptionQuestion3 consumptionQ3 = toEnum(ConsumptionQuestion3.class, request.consumptionQ3());
        EnergyQuestion1 energyQ1 = toEnum(EnergyQuestion1.class, request.energyQ1());
        EnergyQuestion2 energyQ2 = toEnum(EnergyQuestion2.class, request.energyQ2());
        EnergyQuestion3 energyQ3 = toEnum(EnergyQuestion3.class, request.energyQ3());
        DecisionQuestion1 decisionQ1 = toEnum(DecisionQuestion1.class, request.decisionQ1());
        DecisionQuestion2 decisionQ2 = toEnum(DecisionQuestion2.class, request.decisionQ2());
        DecisionQuestion3 decisionQ3 = toEnum(DecisionQuestion3.class, request.decisionQ3());
        RecordStyleQuestion recordStyle = toEnum(RecordStyleQuestion.class, request.recordStyle());

        return new ParsedSurveyData(
                rhythmQ1, rhythmQ2, rhythmQ3,
                consumptionQ1, consumptionQ2, consumptionQ3,
                energyQ1, energyQ2, energyQ3,
                decisionQ1, decisionQ2, decisionQ3,
                recordStyle,
                activityTags
        );
    }

    private <E extends Enum<E> & CodedEnum> E toEnum(Class<E> enumClass, Integer code) {
        return EnumUtils.fromCode(enumClass, code)
                .orElseThrow(InvalidSurveyAnswerCodeException::new);
    }

    private <E extends Enum<E> & CodedEnum> List<E> toEnumList(Class<E> enumClass, List<Integer> codes) {
        return codes.stream()
                .map(code -> toEnum(enumClass, code))
                .collect(Collectors.toList());
    }
}
