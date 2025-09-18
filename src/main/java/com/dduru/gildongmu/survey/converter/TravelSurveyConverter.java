package com.dduru.gildongmu.survey.converter;

import com.dduru.gildongmu.survey.domain.TravelSurvey;
import com.dduru.gildongmu.survey.domain.enums.CaptureStyle;
import com.dduru.gildongmu.survey.domain.enums.ExpenseStyle;
import com.dduru.gildongmu.survey.domain.enums.MoveStyle;
import com.dduru.gildongmu.survey.domain.enums.PaceStyle;
import com.dduru.gildongmu.survey.domain.enums.PlanStyle;
import com.dduru.gildongmu.survey.domain.enums.SpendStyle;
import com.dduru.gildongmu.survey.domain.enums.StayStyle;
import com.dduru.gildongmu.survey.domain.enums.TastingStyle;
import com.dduru.gildongmu.survey.dto.TravelSurveyRequest;
import org.springframework.stereotype.Component;

@Component
public class TravelSurveyConverter {

    public TravelSurvey toEntity(Long userId, TravelSurveyRequest request) {
        return TravelSurvey.builder()
                .userId(userId)
                .planStyle(PlanStyle.valueOf(request.planStyle()))
                .tastingStyle(TastingStyle.valueOf(request.tastingStyle()))
                .stayStyle(StayStyle.valueOf(request.stayStyle()))
                .expenseStyle(ExpenseStyle.valueOf(request.expenseStyle()))
                .moveStyle(MoveStyle.valueOf(request.moveStyle()))
                .spendStyle(SpendStyle.valueOf(request.spendStyle()))
                .captureStyle(CaptureStyle.valueOf(request.captureStyle()))
                .paceStyle(PaceStyle.valueOf(request.paceStyle()))
                .build();
    }
}
