package com.dduru.gildongmu.survey.domain;

import com.dduru.gildongmu.common.entity.BaseTimeEntity;
import com.dduru.gildongmu.survey.domain.enums.Interest;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "travel_survey_interests")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TravelSurveyInterest extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "travel_survey_id", nullable = false)
    private TravelSurvey travelSurvey;

    private Interest interest;
}
