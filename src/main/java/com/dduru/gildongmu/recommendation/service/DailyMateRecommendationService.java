package com.dduru.gildongmu.recommendation.service;

import com.dduru.gildongmu.recommendation.domain.enums.MateRecommendationBatchStatus;
import com.dduru.gildongmu.recommendation.dto.query.RecommendationApplicantContext;
import com.dduru.gildongmu.recommendation.dto.result.DailyMateRecommendationResult;
import com.dduru.gildongmu.recommendation.dto.result.PostRecommendationResult;
import com.dduru.gildongmu.recommendation.dto.result.RecommendationBatchClaimResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DailyMateRecommendationService {

    private static final String BATCH_UNIQUE_KEY = "uk_mate_recommendation_batches_user_date";

    private final RecommendationApplicantContextResolver applicantContextResolver;
    private final RecommendationBatchClaimService batchClaimService;
    private final PostRecommendationSelectionService selectionService;
    private final RecommendationBatchCompletionService completionService;
    private final RecommendationBatchFailureService failureService;

    public DailyMateRecommendationResult getOrCreate(Long userId) {
        Optional<RecommendationApplicantContext> resolvedContext = applicantContextResolver.resolve(userId);
        if (resolvedContext.isEmpty()) {
            return DailyMateRecommendationResult.surveyRequired();
        }

        RecommendationApplicantContext context = resolvedContext.get();
        RecommendationBatchClaimResult claim = claim(userId, context);
        if (!claim.claimed()) {
            log.info("일일 추천 묶음 재사용 - userId={}, batchId={}, status={}", userId, claim.batchId(), claim.status());
            return DailyMateRecommendationResult.available(claim.batchId(), claim.status(), context);
        }

        log.info("일일 추천 묶음 생성 시작 - userId={}, batchId={}, date={}", userId, claim.batchId(), context.today());
        try {
            PostRecommendationResult selection = selectionService.selectRecommendations(userId, context);
            completionService.complete(claim.batchId(), selection);
            MateRecommendationBatchStatus completedStatus = selection.recommendations().isEmpty()
                    ? MateRecommendationBatchStatus.EMPTY
                    : MateRecommendationBatchStatus.COMPLETED;
            log.info("일일 추천 묶음 생성 완료 - userId={}, batchId={}, status={}, count={}",
                    userId, claim.batchId(), completedStatus, selection.recommendations().size());
            return DailyMateRecommendationResult.available(claim.batchId(), completedStatus, context);
        } catch (RuntimeException e) {
            try {
                failureService.markFailed(claim.batchId(), e);
            } catch (RuntimeException failureMarkException) {
                e.addSuppressed(failureMarkException);
                log.error("추천 묶음 실패 상태 기록 실패 - userId={}, batchId={}",
                        userId, claim.batchId(), failureMarkException);
            }
            log.error("일일 추천 묶음 생성 실패 - userId={}, batchId={}", userId, claim.batchId(), e);
            throw e;
        }
    }

    private RecommendationBatchClaimResult claim(Long userId, RecommendationApplicantContext context) {
        try {
            return batchClaimService.claim(userId, context.today());
        } catch (DataIntegrityViolationException e) {
            if (!isBatchUniqueViolation(e)) {
                throw e;
            }
            log.info("일일 추천 묶음 동시 생성 감지 - userId={}, date={}", userId, context.today());
            return batchClaimService.findExisting(userId, context.today());
        }
    }

    private boolean isBatchUniqueViolation(DataIntegrityViolationException exception) {
        for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
            if (cause instanceof ConstraintViolationException constraintViolation
                    && isBatchUniqueConstraint(constraintViolation.getConstraintName())) {
                return true;
            }
        }

        for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
            String message = cause.getMessage();
            if (message != null && message.toLowerCase(Locale.ROOT).contains(BATCH_UNIQUE_KEY)) {
                return true;
            }
        }
        return false;
    }

    private boolean isBatchUniqueConstraint(String constraintName) {
        return constraintName != null && constraintName.equalsIgnoreCase(BATCH_UNIQUE_KEY);
    }
}
