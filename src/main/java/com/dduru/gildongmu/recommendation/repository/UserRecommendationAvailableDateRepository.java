package com.dduru.gildongmu.recommendation.repository;

import com.dduru.gildongmu.recommendation.domain.UserRecommendationAvailableDate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRecommendationAvailableDateRepository extends JpaRepository<UserRecommendationAvailableDate, Long> {
    List<UserRecommendationAvailableDate> findAllByUser_Id(Long userId);
}
