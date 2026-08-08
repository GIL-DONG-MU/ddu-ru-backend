package com.dduru.gildongmu.home.service;

import com.dduru.gildongmu.home.dto.response.MateRecommendationResponse;
import com.dduru.gildongmu.home.mapper.HomeRecommendationMapper;
import com.dduru.gildongmu.recommendation.dto.result.MateRecommendationQueryResult;
import com.dduru.gildongmu.recommendation.service.DailyMateRecommendationQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HomeRecommendationQueryService {

    private final DailyMateRecommendationQueryService dailyMateRecommendationQueryService;
    private final HomeRecommendationMapper homeRecommendationMapper;

    public MateRecommendationResponse retrieve(Long userId) {
        MateRecommendationQueryResult result = dailyMateRecommendationQueryService.retrieve(userId);
        if (result.requiresSurvey()) {
            return MateRecommendationResponse.surveyRequired();
        }
        if (result.isGenerating()) {
            return MateRecommendationResponse.generating();
        }

        return MateRecommendationResponse.available(
                result.recommendations().stream()
                        .map(card -> homeRecommendationMapper.toResponse(card, result.referenceDate()))
                        .toList()
        );
    }
}
