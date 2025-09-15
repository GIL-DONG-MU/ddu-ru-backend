package com.dduru.gildongmu.survey.repository;

import com.dduru.gildongmu.survey.domain.TravelSurvey;
import com.dduru.gildongmu.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TravelSurveyRepository extends JpaRepository<TravelSurvey, Long> {
    Optional<TravelSurvey> findByUser(User user);
}
