package com.dduru.gildongmu.survey.converter;

import com.dduru.gildongmu.survey.domain.enums.ExpenseStyle;
import com.dduru.gildongmu.survey.exception.UnknownSurveyAnswerException;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class ExpenseStyleConverter implements AttributeConverter<ExpenseStyle, Integer> {

    @Override
    public Integer convertToDatabaseColumn(ExpenseStyle attribute) {
        return attribute == null ? null : attribute.getCode();
    }

    @Override
    public ExpenseStyle convertToEntityAttribute(Integer dbData) {
        if (dbData == null) return null;
        return ExpenseStyle.fromCode(dbData)
                .orElseThrow(() -> new UnknownSurveyAnswerException(dbData));
    }
}
