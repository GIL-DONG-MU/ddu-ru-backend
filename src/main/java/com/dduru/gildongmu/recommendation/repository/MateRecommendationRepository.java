package com.dduru.gildongmu.recommendation.repository;

import com.dduru.gildongmu.recommendation.domain.MateRecommendation;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MateRecommendationRepository extends JpaRepository<MateRecommendation, Long> {

    List<MateRecommendation> findAllByBatch_IdOrderByRecommendationRankAsc(Long batchId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT r
            FROM MateRecommendation r
            JOIN FETCH r.batch b
            JOIN FETCH r.post p
            WHERE r.id = :recommendationId
              AND b.user.id = :userId
            """)
    Optional<MateRecommendation> findOwnedByIdWithLock(
            @Param("recommendationId") Long recommendationId,
            @Param("userId") Long userId
    );
}
