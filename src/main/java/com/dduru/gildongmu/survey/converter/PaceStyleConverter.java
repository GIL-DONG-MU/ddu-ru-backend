package com.dduru.gildongmu.survey.converter;

import com.dduru.gildongmu.survey.domain.enums.PaceStyle;
import com.dduru.gildongmu.survey.exception.UnknownSurveyAnswerException;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class PaceStyleConverter implements AttributeConverter<PaceStyle, Integer> {

    @Override
    public Integer convertToDatabaseColumn(PaceStyle attribute) {
        return attribute == null ? null : attribute.getCode();
    }

    @Override
    public PaceStyle convertToEntityAttribute(Integer dbData) {
        if (dbData == null) return null;
        return PaceStyle.fromCode(dbData)
                .orElseThrow(() -> new UnknownSurveyAnswerException(dbData));
    }
}
