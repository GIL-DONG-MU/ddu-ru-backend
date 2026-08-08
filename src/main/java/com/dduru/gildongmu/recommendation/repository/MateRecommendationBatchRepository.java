package com.dduru.gildongmu.recommendation.repository;

import com.dduru.gildongmu.recommendation.domain.MateRecommendationBatch;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface MateRecommendationBatchRepository extends JpaRepository<MateRecommendationBatch, Long> {

    Optional<MateRecommendationBatch> findByUser_IdAndRecommendationDate(Long userId, LocalDate recommendationDate);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT b
            FROM MateRecommendationBatch b
            WHERE b.user.id = :userId
              AND b.recommendationDate = :recommendationDate
            """)
    Optional<MateRecommendationBatch> findByUserIdAndRecommendationDateWithLock(
            @Param("userId") Long userId,
            @Param("recommendationDate") LocalDate recommendationDate
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM MateRecommendationBatch b WHERE b.id = :id")
    Optional<MateRecommendationBatch> findByIdWithLock(@Param("id") Long id);
}
