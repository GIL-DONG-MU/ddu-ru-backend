package com.dduru.gildongmu.recommendation.repository;

import com.dduru.gildongmu.recommendation.domain.UserRecommendationAvailableDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRecommendationAvailableDateRepository extends JpaRepository<UserRecommendationAvailableDate, Long> {
    List<UserRecommendationAvailableDate> findAllByUser_Id(Long userId);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM UserRecommendationAvailableDate d WHERE d.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}
