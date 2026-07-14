package com.dduru.gildongmu.recommendation.service;

import com.dduru.gildongmu.recommendation.domain.MateRecommendationBatch;
import com.dduru.gildongmu.recommendation.domain.enums.MateRecommendationBatchStatus;
import com.dduru.gildongmu.recommendation.repository.MateRecommendationBatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RecommendationBatchFailureService {

    private final MateRecommendationBatchRepository batchRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(Long batchId, Throwable throwable) {
        MateRecommendationBatch batch = batchRepository.findByIdForUpdate(batchId).orElse(null);
        if (batch == null || batch.getStatus() != MateRecommendationBatchStatus.CREATED) {
            return;
        }
        batch.fail(failureReason(throwable));
    }

    private String failureReason(Throwable throwable) {
        String message = throwable.getMessage();
        return throwable.getClass().getSimpleName() + (message == null ? "" : ": " + message);
    }
}
