package com.dduru.gildongmu.survey.domain;

import com.dduru.gildongmu.common.entity.BaseTimeEntity;
import com.dduru.gildongmu.survey.domain.enums.*;
import com.dduru.gildongmu.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
}
