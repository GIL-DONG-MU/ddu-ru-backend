package com.dduru.gildongmu.survey.domain;

import com.dduru.gildongmu.common.entity.BaseTimeEntity;
import com.dduru.gildongmu.survey.domain.enums.*;
import com.dduru.gildongmu.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "travel_surveys")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TravelSurvey extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private PlanStyle planStyle;

    @Enumerated(EnumType.STRING)
    private TastingStyle tastingStyle;

    @Enumerated(EnumType.STRING)
    private StayStyle stayStyle;

    @Enumerated(EnumType.STRING)
    private ExpenseStyle expenseStyle;

    @Enumerated(EnumType.STRING)
    private MoveStyle moveStyle;

    @Enumerated(EnumType.STRING)
    private SpendStyle spendStyle;

    @Enumerated(EnumType.STRING)
    private CaptureStyle captureStyle;

    @Enumerated(EnumType.STRING)
    private PaceStyle paceStyle;

    @OneToMany(mappedBy = "travelSurvey", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<TravelSurveyInterest> interests = new HashSet<>();
}

