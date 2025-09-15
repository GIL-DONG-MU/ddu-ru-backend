package com.dduru.gildongmu.survey.converter;

import com.dduru.gildongmu.survey.domain.enums.CaptureStyle;
import com.dduru.gildongmu.survey.exception.UnknownSurveyAnswerException;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class CaptureStyleConverter implements AttributeConverter<CaptureStyle, Integer> {

    @Override
    public Integer convertToDatabaseColumn(CaptureStyle attribute) {
        return attribute == null ? null : attribute.getCode();
    }

    @Override
    public CaptureStyle convertToEntityAttribute(Integer dbData) {
        if (dbData == null) return null;
        return CaptureStyle.fromCode(dbData)
                .orElseThrow(() -> new UnknownSurveyAnswerException(dbData));
    }
}
