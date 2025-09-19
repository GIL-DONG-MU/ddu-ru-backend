package com.dduru.gildongmu.survey.repository;

import com.dduru.gildongmu.survey.domain.TravelSurvey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TravelSurveyRepository extends JpaRepository<TravelSurvey, Long> {
    Optional<TravelSurvey> findByUserId(Long userId);
}
