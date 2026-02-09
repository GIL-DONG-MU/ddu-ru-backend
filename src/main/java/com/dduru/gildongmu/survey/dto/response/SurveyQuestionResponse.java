package com.dduru.gildongmu.survey.dto.response;

import com.dduru.gildongmu.survey.domain.enums.SurveyQuestionType;

import java.util.List;

public record SurveyQuestionResponse(
        String id,
        Integer displayOrder,
        String questionText,
        String imageUrl,
        SurveyQuestionType type,
        boolean required,
        Integer minSelect,
        Integer maxSelect,
        List<SurveyQuestionOptionResponse> options
) {
}
