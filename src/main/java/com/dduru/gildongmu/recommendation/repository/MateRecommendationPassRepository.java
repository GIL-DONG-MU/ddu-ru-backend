package com.dduru.gildongmu.recommendation.repository;

import com.dduru.gildongmu.recommendation.domain.MateRecommendationPass;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MateRecommendationPassRepository extends JpaRepository<MateRecommendationPass, Long> {
    boolean existsByUser_IdAndPost_Id(Long userId, Long postId);
}
