package com.dduru.gildongmu.recommendation.service;

import com.dduru.gildongmu.profile.domain.enums.Gender;
import com.dduru.gildongmu.recommendation.domain.enums.MateRecommendationBatchStatus;
import com.dduru.gildongmu.recommendation.domain.enums.RecommendationAvailabilityStatus;
import com.dduru.gildongmu.recommendation.dto.query.DestinationPreferenceFilter;
import com.dduru.gildongmu.recommendation.dto.query.RecommendationApplicantContext;
import com.dduru.gildongmu.recommendation.dto.result.DailyMateRecommendationResult;
import com.dduru.gildongmu.recommendation.dto.result.MateRecommendationQueryResult;
import com.dduru.gildongmu.recommendation.support.TravelTendencyScores;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@DisplayName("MateRecommendationQueryService 테스트")
class MateRecommendationQueryServiceTest {

    private DailyMateRecommendationService dailyMateRecommendationService;
    private MateRecommendationCardQueryService cardQueryService;
    private MateRecommendationQueryService queryService;

    @BeforeEach
    void setUp() {
        dailyMateRecommendationService = mock(DailyMateRecommendationService.class);
        cardQueryService = mock(MateRecommendationCardQueryService.class);
        queryService = new MateRecommendationQueryService(dailyMateRecommendationService, cardQueryService);
    }

    @Test
    @DisplayName("설문 미완료 사용자는 추천 카드를 조회하지 않는다")
    void surveyRequired() {
        when(dailyMateRecommendationService.getOrCreate(10L))
                .thenReturn(DailyMateRecommendationResult.surveyRequired());

        MateRecommendationQueryResult result = queryService.retrieve(10L);

        assertThat(result.availabilityStatus()).isEqualTo(RecommendationAvailabilityStatus.SURVEY_REQUIRED);
        assertThat(result.recommendations()).isEmpty();
        verifyNoInteractions(cardQueryService);
    }

    @Test
    @DisplayName("추천 묶음이 비어 있으면 빈 추천 목록을 반환한다")
    void emptyBatch() {
        when(dailyMateRecommendationService.getOrCreate(10L)).thenReturn(
                DailyMateRecommendationResult.available(1L, MateRecommendationBatchStatus.EMPTY, null)
        );

        MateRecommendationQueryResult result = queryService.retrieve(10L);

        assertThat(result.availabilityStatus()).isEqualTo(RecommendationAvailabilityStatus.AVAILABLE);
        assertThat(result.recommendations()).isEmpty();
        verifyNoInteractions(cardQueryService);
    }

    @Test
    @DisplayName("완료된 추천 묶음은 노출 가능한 카드를 조회한다")
    void completedBatch() {
        LocalDate today = LocalDate.of(2026, 5, 13);
        RecommendationApplicantContext context = new RecommendationApplicantContext(
                today,
                Gender.F,
                27,
                new DestinationPreferenceFilter(Set.of(), Set.of()),
                List.of(),
                new TravelTendencyScores(5, 5, 5, 5)
        );
        when(dailyMateRecommendationService.getOrCreate(10L)).thenReturn(
                DailyMateRecommendationResult.available(1L, MateRecommendationBatchStatus.COMPLETED, context)
        );
        when(cardQueryService.findVisibleCards(1L, 10L, context)).thenReturn(List.of());

        MateRecommendationQueryResult result = queryService.retrieve(10L);

        assertThat(result.referenceDate()).isEqualTo(today);
        assertThat(result.recommendations()).isEmpty();
    }
}
