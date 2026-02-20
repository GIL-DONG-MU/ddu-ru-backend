package com.dduru.gildongmu.survey.repository;

import com.dduru.gildongmu.survey.domain.Survey;
import com.dduru.gildongmu.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SurveyRepository extends JpaRepository<Survey, Long> {
    Optional<Survey> findByUser(User user);
}
