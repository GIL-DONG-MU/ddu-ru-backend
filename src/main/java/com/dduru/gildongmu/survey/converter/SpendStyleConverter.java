package com.dduru.gildongmu.survey.converter;

import com.dduru.gildongmu.survey.domain.enums.SpendStyle;
import com.dduru.gildongmu.survey.exception.UnknownSurveyAnswerException;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class SpendStyleConverter implements AttributeConverter<SpendStyle, Integer> {

    @Override
    public Integer convertToDatabaseColumn(SpendStyle attribute) {
        return attribute == null ? null : attribute.getCode();
    }

    @Override
    public SpendStyle convertToEntityAttribute(Integer dbData) {
        if (dbData == null) return null;
        return SpendStyle.fromCode(dbData)
                .orElseThrow(() -> new UnknownSurveyAnswerException(dbData));
    }
}
