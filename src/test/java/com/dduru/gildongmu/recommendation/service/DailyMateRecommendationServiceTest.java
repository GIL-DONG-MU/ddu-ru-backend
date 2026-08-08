package com.dduru.gildongmu.recommendation.service;

import com.dduru.gildongmu.profile.domain.enums.Gender;
import com.dduru.gildongmu.recommendation.domain.enums.MateRecommendationBatchStatus;
import com.dduru.gildongmu.recommendation.domain.enums.RecommendationAvailabilityStatus;
import com.dduru.gildongmu.recommendation.dto.query.DestinationPreferenceFilter;
import com.dduru.gildongmu.recommendation.dto.query.RecommendationApplicantContext;
import com.dduru.gildongmu.recommendation.dto.result.DailyMateRecommendationResult;
import com.dduru.gildongmu.recommendation.dto.result.PostRecommendationResult;
import com.dduru.gildongmu.recommendation.dto.result.RecommendationBatchClaimResult;
import com.dduru.gildongmu.recommendation.dto.result.ScoredPostRecommendation;
import com.dduru.gildongmu.recommendation.support.TravelTendencyScores;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@DisplayName("DailyMateRecommendationService 테스트")
class DailyMateRecommendationServiceTest {

    private RecommendationApplicantContextResolver contextResolver;
    private RecommendationBatchClaimService claimService;
    private PostRecommendationSelectionService selectionService;
    private RecommendationBatchCompletionService completionService;
    private RecommendationBatchFailureService failureService;
    private DailyMateRecommendationService service;

    @BeforeEach
    void setUp() {
        contextResolver = mock(RecommendationApplicantContextResolver.class);
        claimService = mock(RecommendationBatchClaimService.class);
        selectionService = mock(PostRecommendationSelectionService.class);
        completionService = mock(RecommendationBatchCompletionService.class);
        failureService = mock(RecommendationBatchFailureService.class);
        service = new DailyMateRecommendationService(
                contextResolver,
                claimService,
                selectionService,
                completionService,
                failureService
        );
    }

    @Test
    @DisplayName("설문 미완료 사용자는 배치를 생성하지 않는다")
    void surveyRequiredDoesNotClaimBatch() {
        when(contextResolver.resolve(1L)).thenReturn(Optional.empty());

        DailyMateRecommendationResult result = service.getOrCreate(1L);

        assertThat(result.availabilityStatus()).isEqualTo(RecommendationAvailabilityStatus.SURVEY_REQUIRED);
        verifyNoInteractions(claimService, selectionService, completionService, failureService);
    }

    @Test
    @DisplayName("완료된 당일 배치는 후보를 다시 계산하지 않고 재사용한다")
    void reusesCompletedBatch() {
        RecommendationApplicantContext context = context();
        when(contextResolver.resolve(1L)).thenReturn(Optional.of(context));
        when(claimService.claim(1L, context.today())).thenReturn(
                RecommendationBatchClaimResult.existing(10L, MateRecommendationBatchStatus.COMPLETED)
        );

        DailyMateRecommendationResult result = service.getOrCreate(1L);

        assertThat(result.batchId()).isEqualTo(10L);
        assertThat(result.batchStatus()).isEqualTo(MateRecommendationBatchStatus.COMPLETED);
        verifyNoInteractions(selectionService, completionService, failureService);
    }

    @Test
    @DisplayName("KST 날짜가 바뀌면 다음 날짜의 새 묶음을 생성한다")
    void createsNewBatchOnNextDay() {
        RecommendationApplicantContext today = context();
        RecommendationApplicantContext tomorrow = new RecommendationApplicantContext(
                today.today().plusDays(1),
                today.gender(),
                today.age(),
                today.destinationPreferenceFilter(),
                today.availableDateRanges(),
                today.applicantScores()
        );
        when(contextResolver.resolve(1L)).thenReturn(Optional.of(today), Optional.of(tomorrow));
        when(claimService.claim(1L, today.today())).thenReturn(
                RecommendationBatchClaimResult.existing(10L, MateRecommendationBatchStatus.EMPTY)
        );
        when(claimService.claim(1L, tomorrow.today())).thenReturn(RecommendationBatchClaimResult.claimed(11L));
        when(selectionService.selectRecommendations(1L, tomorrow)).thenReturn(PostRecommendationResult.noCandidates());

        DailyMateRecommendationResult first = service.getOrCreate(1L);
        DailyMateRecommendationResult second = service.getOrCreate(1L);

        assertThat(first.batchId()).isEqualTo(10L);
        assertThat(second.batchId()).isEqualTo(11L);
        verify(completionService).complete(11L, PostRecommendationResult.noCandidates());
    }

    @Test
    @DisplayName("후보가 없으면 EMPTY로 완료한다")
    void completesAsEmpty() {
        RecommendationApplicantContext context = context();
        PostRecommendationResult selection = PostRecommendationResult.noCandidates();
        when(contextResolver.resolve(1L)).thenReturn(Optional.of(context));
        when(claimService.claim(1L, context.today())).thenReturn(RecommendationBatchClaimResult.claimed(10L));
        when(selectionService.selectRecommendations(1L, context)).thenReturn(selection);

        DailyMateRecommendationResult result = service.getOrCreate(1L);

        assertThat(result.batchStatus()).isEqualTo(MateRecommendationBatchStatus.EMPTY);
        verify(completionService).complete(10L, selection);
    }

    @Test
    @DisplayName("후보가 있으면 COMPLETED로 완료한다")
    void completesWithRecommendations() {
        RecommendationApplicantContext context = context();
        PostRecommendationResult selection = PostRecommendationResult.ready(List.of(
                new ScoredPostRecommendation(100L, context.today().plusDays(1), context.today().plusDays(2), 90, List.of(), List.of())
        ));
        when(contextResolver.resolve(1L)).thenReturn(Optional.of(context));
        when(claimService.claim(1L, context.today())).thenReturn(RecommendationBatchClaimResult.claimed(10L));
        when(selectionService.selectRecommendations(1L, context)).thenReturn(selection);

        DailyMateRecommendationResult result = service.getOrCreate(1L);

        assertThat(result.batchStatus()).isEqualTo(MateRecommendationBatchStatus.COMPLETED);
        verify(completionService).complete(10L, selection);
    }

    @Test
    @DisplayName("생성 중 예외가 발생하면 배치를 FAILED로 기록하고 예외를 다시 던진다")
    void marksFailedAndRethrows() {
        RecommendationApplicantContext context = context();
        RuntimeException failure = new RuntimeException("selection failed");
        when(contextResolver.resolve(1L)).thenReturn(Optional.of(context));
        when(claimService.claim(1L, context.today())).thenReturn(RecommendationBatchClaimResult.claimed(10L));
        when(selectionService.selectRecommendations(1L, context)).thenThrow(failure);

        assertThatThrownBy(() -> service.getOrCreate(1L)).isSameAs(failure);
        verify(failureService).markFailed(10L, failure);
    }

    @Test
    @DisplayName("constraint 이름으로 동시 생성 unique 충돌을 판별해 기존 묶음을 재사용한다")
    void reusesBatchAfterConstraintNameUniqueConflict() {
        RecommendationApplicantContext context = context();
        when(contextResolver.resolve(1L)).thenReturn(Optional.of(context));
        when(claimService.claim(1L, context.today())).thenThrow(
                new DataIntegrityViolationException(
                        "could not execute statement",
                        new ConstraintViolationException(
                                "duplicate entry",
                                new SQLException("duplicate key"),
                                "uk_mate_recommendation_batches_user_date"
                        )
                )
        );
        when(claimService.findExisting(1L, context.today())).thenReturn(
                RecommendationBatchClaimResult.existing(10L, MateRecommendationBatchStatus.CREATED)
        );

        DailyMateRecommendationResult result = service.getOrCreate(1L);

        assertThat(result.batchId()).isEqualTo(10L);
        assertThat(result.batchStatus()).isEqualTo(MateRecommendationBatchStatus.CREATED);
        assertThat(result.availabilityStatus()).isEqualTo(RecommendationAvailabilityStatus.GENERATING);
        verifyNoInteractions(selectionService, completionService, failureService);
    }

    @Test
    @DisplayName("constraint 이름을 얻을 수 없으면 예외 메시지로 unique 충돌을 판별한다")
    void reusesBatchAfterMessageFallbackUniqueConflict() {
        RecommendationApplicantContext context = context();
        when(contextResolver.resolve(1L)).thenReturn(Optional.of(context));
        when(claimService.claim(1L, context.today())).thenThrow(
                new DataIntegrityViolationException("uk_mate_recommendation_batches_user_date")
        );
        when(claimService.findExisting(1L, context.today())).thenReturn(
                RecommendationBatchClaimResult.existing(10L, MateRecommendationBatchStatus.CREATED)
        );

        DailyMateRecommendationResult result = service.getOrCreate(1L);

        assertThat(result.batchId()).isEqualTo(10L);
        assertThat(result.availabilityStatus()).isEqualTo(RecommendationAvailabilityStatus.GENERATING);
        verifyNoInteractions(selectionService, completionService, failureService);
    }

    @Test
    @DisplayName("다른 constraint 위반은 기존 예외를 그대로 전파한다")
    void rethrowsOtherConstraintViolation() {
        RecommendationApplicantContext context = context();
        DataIntegrityViolationException failure = new DataIntegrityViolationException(
                "could not execute statement",
                new ConstraintViolationException(
                        "duplicate entry",
                        new SQLException("duplicate key"),
                        "uk_other_constraint"
                )
        );
        when(contextResolver.resolve(1L)).thenReturn(Optional.of(context));
        when(claimService.claim(1L, context.today())).thenThrow(failure);

        assertThatThrownBy(() -> service.getOrCreate(1L)).isSameAs(failure);
        verify(claimService, never()).findExisting(anyLong(), any(LocalDate.class));
        verifyNoInteractions(selectionService, completionService, failureService);
    }

    private RecommendationApplicantContext context() {
        return new RecommendationApplicantContext(
                LocalDate.of(2026, 7, 15),
                Gender.F,
                27,
                new DestinationPreferenceFilter(Set.of(), Set.of()),
                List.of(),
                new TravelTendencyScores(5, 5, 5, 5)
        );
    }
}
