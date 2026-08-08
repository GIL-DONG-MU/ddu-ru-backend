package com.dduru.gildongmu.recommendation.service;

import com.dduru.gildongmu.recommendation.dto.query.MateRecommendationCardQueryResult;
import com.dduru.gildongmu.recommendation.dto.query.RecommendationApplicantContext;
import com.dduru.gildongmu.recommendation.repository.MateRecommendationCardQueryRepository;
import com.dduru.gildongmu.recommendation.support.RecommendationAvailableDateMatcher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VisibleMateRecommendationCardQueryService {

    private final MateRecommendationCardQueryRepository cardQueryRepository;
    private final RecommendationAvailableDateMatcher availableDateMatcher;

    @Transactional(readOnly = true)
    public List<MateRecommendationCardQueryResult> findVisibleCards(
            Long batchId,
            Long userId,
            RecommendationApplicantContext context
    ) {
        return cardQueryRepository.findVisibleCards(batchId, userId, context).stream()
                .filter(card -> availableDateMatcher.matches(
                        card.companionType(),
                        card.startDate(),
                        card.endDate(),
                        context.availableDateRanges()
                ))
                .toList();
    }
}
