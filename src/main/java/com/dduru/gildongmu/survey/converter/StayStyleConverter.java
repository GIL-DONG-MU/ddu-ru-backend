package com.dduru.gildongmu.survey.converter;

import com.dduru.gildongmu.survey.domain.enums.StayStyle;
import com.dduru.gildongmu.survey.exception.UnknownSurveyAnswerException;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class StayStyleConverter implements AttributeConverter<StayStyle, Integer> {

    @Override
    public Integer convertToDatabaseColumn(StayStyle attribute) {
        return attribute == null ? null : attribute.getCode();
    }

    @Override
    public StayStyle convertToEntityAttribute(Integer dbData) {
        if (dbData == null) return null;
        return StayStyle.fromCode(dbData)
                .orElseThrow(() -> new UnknownSurveyAnswerException(dbData));
    }
}
