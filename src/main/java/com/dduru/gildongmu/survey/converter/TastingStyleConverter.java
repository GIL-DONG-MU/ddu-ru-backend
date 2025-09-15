package com.dduru.gildongmu.survey.converter;

import com.dduru.gildongmu.survey.domain.enums.TastingStyle;
import com.dduru.gildongmu.survey.exception.UnknownSurveyAnswerException;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class TastingStyleConverter implements AttributeConverter<TastingStyle, Integer> {

    @Override
    public Integer convertToDatabaseColumn(TastingStyle attribute) {
        return attribute == null ? null : attribute.getCode();
    }

    @Override
    public TastingStyle convertToEntityAttribute(Integer dbData) {
        if (dbData == null) return null;
        return TastingStyle.fromCode(dbData)
                .orElseThrow(() -> new UnknownSurveyAnswerException(dbData));
    }
}
