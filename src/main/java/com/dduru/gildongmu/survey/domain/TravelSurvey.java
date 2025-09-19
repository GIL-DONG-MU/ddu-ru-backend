package com.dduru.gildongmu.survey.domain;

import com.dduru.gildongmu.common.entity.BaseTimeEntity;
import com.dduru.gildongmu.survey.domain.enums.CaptureStyle;
import com.dduru.gildongmu.survey.domain.enums.ExpenseStyle;
import com.dduru.gildongmu.survey.domain.enums.MoveStyle;
import com.dduru.gildongmu.survey.domain.enums.PaceStyle;
import com.dduru.gildongmu.survey.domain.enums.PlanStyle;
import com.dduru.gildongmu.survey.domain.enums.SpendStyle;
import com.dduru.gildongmu.survey.domain.enums.StayStyle;
import com.dduru.gildongmu.survey.domain.enums.TastingStyle;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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

    private Long userId;

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

    public void updateStyles(
            PlanStyle planStyle, TastingStyle tastingStyle,
            StayStyle stayStyle, ExpenseStyle expenseStyle, MoveStyle moveStyle,
            SpendStyle spendStyle, CaptureStyle captureStyle, PaceStyle paceStyle
    ) {
        this.planStyle = planStyle;
        this.tastingStyle = tastingStyle;
        this.stayStyle = stayStyle;
        this.expenseStyle = expenseStyle;
        this.moveStyle = moveStyle;
        this.spendStyle = spendStyle;
        this.captureStyle = captureStyle;
        this.paceStyle = paceStyle;
    }
}
