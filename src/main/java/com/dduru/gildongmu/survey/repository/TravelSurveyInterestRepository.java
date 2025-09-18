package com.dduru.gildongmu.survey.repository;

import com.dduru.gildongmu.survey.domain.TravelSurvey;
import com.dduru.gildongmu.survey.domain.TravelSurveyInterest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TravelSurveyInterestRepository extends JpaRepository<TravelSurveyInterest, Long> {
    List<TravelSurveyInterest> findByTravelSurvey(TravelSurvey travelSurvey);
}
