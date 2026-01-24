package com.dduru.gildongmu.survey.dto;

import java.util.List;

public record SurveyQuestionListResponse(
        int version,
        int count,
        List<SurveyQuestionResponse> questions
) {
}
