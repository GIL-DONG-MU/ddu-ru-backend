package com.dduru.gildongmu.survey.repository;

import com.dduru.gildongmu.survey.domain.SurveyQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SurveyQuestionRepository extends JpaRepository<SurveyQuestion, String> {

    List<SurveyQuestion> findAllByOrderByDisplayOrderAsc();

    @Query("select max(q.modifiedAt) from SurveyQuestion q")
    LocalDateTime findMaxModifiedAt();
}
