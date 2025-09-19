package com.dduru.gildongmu.survey.repository;

import com.dduru.gildongmu.survey.domain.TravelSurvey;
import com.dduru.gildongmu.survey.domain.TravelSurveyInterest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface TravelSurveyInterestRepository extends JpaRepository<TravelSurveyInterest, Long> {
    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM TravelSurveyInterest i WHERE i.travelSurvey = :travelSurvey")
    void deleteAllByTravelSurvey(TravelSurvey travelSurvey);
}
