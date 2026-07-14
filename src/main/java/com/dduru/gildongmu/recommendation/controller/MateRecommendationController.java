package com.dduru.gildongmu.recommendation.controller;

import com.dduru.gildongmu.common.annotation.CurrentUser;
import com.dduru.gildongmu.common.dto.ApiResult;
import com.dduru.gildongmu.recommendation.RecommendationEndpoints;
import com.dduru.gildongmu.recommendation.service.MateRecommendationPassService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(RecommendationEndpoints.MATE_RECOMMENDATIONS)
public class MateRecommendationController implements MateRecommendationApiDocs {

    private final MateRecommendationPassService passService;

    @Override
    @PostMapping(RecommendationEndpoints.PASS)
    public ResponseEntity<ApiResult<Void>> pass(
            @CurrentUser Long userId,
            @PathVariable Long recommendationId
    ) {
        passService.pass(userId, recommendationId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResult.noContent());
    }
}
