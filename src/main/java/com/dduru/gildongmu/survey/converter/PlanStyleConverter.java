package com.dduru.gildongmu.survey.converter;

import com.dduru.gildongmu.survey.domain.enums.PlanStyle;
import com.dduru.gildongmu.survey.exception.UnknownSurveyAnswerException;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class PlanStyleConverter implements AttributeConverter<PlanStyle, Integer> {

    @Override
    public Integer convertToDatabaseColumn(PlanStyle attribute) {
        return attribute == null ? null : attribute.getCode();
    }

    @Override
    public PlanStyle convertToEntityAttribute(Integer dbData) {
        if (dbData == null) return null;
        return PlanStyle.fromCode(dbData)
                .orElseThrow(() -> new UnknownSurveyAnswerException(dbData));
    }
}
