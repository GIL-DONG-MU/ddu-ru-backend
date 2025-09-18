package com.dduru.gildongmu.survey.domain;

import com.dduru.gildongmu.common.entity.BaseTimeEntity;
import com.dduru.gildongmu.survey.domain.enums.Interest;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "travel_survey_interests")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TravelSurveyInterest extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "travel_survey_id", nullable = false)
    private TravelSurvey travelSurvey;

    @Enumerated(EnumType.STRING)
    private Interest interest;

    public static TravelSurveyInterest create(TravelSurvey travelSurvey, Interest interest) {
        return TravelSurveyInterest.builder()
                .travelSurvey(travelSurvey)
                .interest(interest)
                .build();
    }
}
