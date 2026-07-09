package com.dduru.gildongmu.recommendation.repository;

import com.dduru.gildongmu.recommendation.domain.UserRecommendationDestinationPreference;
import com.dduru.gildongmu.recommendation.dto.query.DestinationPreferenceFilterRow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRecommendationDestinationPreferenceRepository extends JpaRepository<UserRecommendationDestinationPreference, Long> {
    @Query("""
            SELECT new com.dduru.gildongmu.recommendation.dto.query.DestinationPreferenceFilterRow(
                preference.preferenceType,
                preference.countryCode,
                preference.destination.id
            )
            FROM UserRecommendationDestinationPreference preference
            WHERE preference.user.id = :userId
            """)
    List<DestinationPreferenceFilterRow> findFilterRowsByUserId(@Param("userId") Long userId);
}
