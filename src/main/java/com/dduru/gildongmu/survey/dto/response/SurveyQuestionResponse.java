package com.dduru.gildongmu.survey.dto.response;

import java.util.List;

public record SurveyQuestionResponse(
        String id,
        Integer displayOrder,
        String questionText,
        String imageUrl,
        String type,
        boolean required,
        Integer minSelect,
        Integer maxSelect,
        List<SurveyQuestionOptionResponse> options
) {
}
