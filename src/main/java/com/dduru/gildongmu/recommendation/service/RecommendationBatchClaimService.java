package com.dduru.gildongmu.recommendation.service;

import com.dduru.gildongmu.recommendation.domain.MateRecommendationBatch;
import com.dduru.gildongmu.recommendation.domain.enums.MateRecommendationBatchStatus;
import com.dduru.gildongmu.recommendation.dto.result.RecommendationBatchClaimResult;
import com.dduru.gildongmu.recommendation.repository.MateRecommendationBatchRepository;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RecommendationBatchClaimService {

    private final MateRecommendationBatchRepository batchRepository;
    private final UserRepository userRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public RecommendationBatchClaimResult claim(Long userId, LocalDate recommendationDate) {
        Optional<MateRecommendationBatch> existing =
                batchRepository.findByUserIdAndRecommendationDateForUpdate(userId, recommendationDate);

        if (existing.isPresent()) {
            MateRecommendationBatch batch = existing.get();
            if (batch.getStatus() == MateRecommendationBatchStatus.FAILED) {
                batch.retry();
                return RecommendationBatchClaimResult.claimed(batch.getId());
            }
            return RecommendationBatchClaimResult.existing(batch.getId(), batch.getStatus());
        }

        User user = userRepository.getByIdOrThrow(userId);
        MateRecommendationBatch batch = batchRepository.saveAndFlush(
                MateRecommendationBatch.create(user, recommendationDate)
        );
        return RecommendationBatchClaimResult.claimed(batch.getId());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public RecommendationBatchClaimResult findExisting(Long userId, LocalDate recommendationDate) {
        MateRecommendationBatch batch = batchRepository
                .findByUser_IdAndRecommendationDate(userId, recommendationDate)
                .orElseThrow(() -> new IllegalStateException("Recommendation batch unique conflict without existing row"));
        return RecommendationBatchClaimResult.existing(batch.getId(), batch.getStatus());
    }
}
