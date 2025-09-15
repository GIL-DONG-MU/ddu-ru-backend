package com.dduru.gildongmu.survey.domain;

import com.dduru.gildongmu.common.entity.BaseTimeEntity;
import com.dduru.gildongmu.survey.converter.*;
import com.dduru.gildongmu.survey.domain.enums.*;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "travel_surveys")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TravelSurvey extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    @Convert(converter = PlanStyleConverter.class)
    private PlanStyle planStyle;

    @Convert(converter = TastingStyleConverter.class)
    private TastingStyle tastingStyle;

    @Convert(converter = StayStyleConverter.class)
    private StayStyle stayStyle;

    @Convert(converter = ExpenseStyleConverter.class)
    private ExpenseStyle expenseStyle;

    @Convert(converter = MoveStyleConverter.class)
    private MoveStyle moveStyle;

    @Convert(converter = SpendStyleConverter.class)
    private SpendStyle spendStyle;

    @Convert(converter = CaptureStyleConverter.class)
    private CaptureStyle captureStyle;

    @Convert(converter = PaceStyleConverter.class)
    private PaceStyle paceStyle;

    @ElementCollection(fetch = FetchType.LAZY)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "travel_survey_interests", joinColumns = @JoinColumn(name = "travel_survey_id"))
    @Column(name = "interest")
    private Set<Interest> interests = new HashSet<>();

    @Lob
    private String scoreSnapshotJson;
}

