package com.dduru.gildongmu.recommendation.repository;

import com.dduru.gildongmu.recommendation.domain.UserRecommendationDestinationPreference;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRecommendationDestinationPreferenceRepository extends JpaRepository<UserRecommendationDestinationPreference, Long> {
    List<UserRecommendationDestinationPreference> findAllByUser_Id(Long userId);
}
