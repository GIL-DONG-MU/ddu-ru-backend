package com.dduru.gildongmu.home.service;

import com.dduru.gildongmu.home.dto.response.MateRecommendationResponse;
import com.dduru.gildongmu.home.mapper.HomeRecommendationMapper;
import com.dduru.gildongmu.recommendation.dto.result.MateRecommendationQueryResult;
import com.dduru.gildongmu.recommendation.service.MateRecommendationQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HomeRecommendationQueryService {

    private final MateRecommendationQueryService mateRecommendationQueryService;
    private final HomeRecommendationMapper homeRecommendationMapper;

    public MateRecommendationResponse retrieve(Long userId) {
        MateRecommendationQueryResult result = mateRecommendationQueryService.retrieve(userId);
        if (result.requiresSurvey()) {
            return MateRecommendationResponse.surveyRequired();
        }

        return MateRecommendationResponse.available(
                result.recommendations().stream()
                        .map(card -> homeRecommendationMapper.toResponse(card, result.referenceDate()))
                        .toList()
        );
    }
}
