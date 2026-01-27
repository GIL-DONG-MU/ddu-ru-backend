package com.dduru.gildongmu.survey.repository;

import com.dduru.gildongmu.survey.domain.SurveyQuestionOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SurveyQuestionOptionRepository extends JpaRepository<SurveyQuestionOption, Long> {

    @Query("""
            select o
            from SurveyQuestionOption o
            join fetch o.question
            where o.question.questionId in :questionIds
            order by o.question.questionId asc, o.displayOrder asc
            """)
    List<SurveyQuestionOption> findOptionsByQuestionIds(@Param("questionIds") List<String> questionIds);

    @Query("select max(o.modifiedAt) from SurveyQuestionOption o")
    LocalDateTime findMaxModifiedAt();
}
