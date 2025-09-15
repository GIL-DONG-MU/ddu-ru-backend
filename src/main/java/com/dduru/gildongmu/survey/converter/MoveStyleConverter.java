package com.dduru.gildongmu.survey.converter;

import com.dduru.gildongmu.survey.domain.enums.MoveStyle;
import com.dduru.gildongmu.survey.exception.UnknownSurveyAnswerException;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class MoveStyleConverter implements AttributeConverter<MoveStyle, Integer> {

    @Override
    public Integer convertToDatabaseColumn(MoveStyle attribute) {
        return attribute == null ? null : attribute.getCode();
    }

    @Override
    public MoveStyle convertToEntityAttribute(Integer dbData) {
        if (dbData == null) return null;
        return MoveStyle.fromCode(dbData)
                .orElseThrow(() -> new UnknownSurveyAnswerException(dbData));
    }
}
