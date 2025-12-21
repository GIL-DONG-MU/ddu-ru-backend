package com.dduru.gildongmu.survey.converter;

import com.dduru.gildongmu.common.enums.EnumUtils;
import com.dduru.gildongmu.survey.domain.Survey;
import com.dduru.gildongmu.survey.domain.enums.*;
import com.dduru.gildongmu.survey.dto.SurveyRequest;
import com.dduru.gildongmu.survey.exception.InvalidSurveyAnswerCodeException;
import com.dduru.gildongmu.user.domain.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SurveyConverter {

    public Survey toEntity(User user, SurveyRequest request) {
        Question1Transport q1 = EnumUtils.fromCode(Question1Transport.class, request.q1())
                .orElseThrow(() -> InvalidSurveyAnswerCodeException.of("Q1 이동수단", request.q1()));

        Question2Waiting q2 = EnumUtils.fromCode(Question2Waiting.class, request.q2())
                .orElseThrow(() -> InvalidSurveyAnswerCodeException.of("Q2 웨이팅", request.q2()));

        Question3Stay q3 = EnumUtils.fromCode(Question3Stay.class, request.q3())
                .orElseThrow(() -> InvalidSurveyAnswerCodeException.of("Q3 숙소", request.q3()));

        Question4Wakeup q4 = EnumUtils.fromCode(Question4Wakeup.class, request.q4())
                .orElseThrow(() -> InvalidSurveyAnswerCodeException.of("Q4 기상시간", request.q4()));

        Question5Expense q5 = EnumUtils.fromCode(Question5Expense.class, request.q5())
                .orElseThrow(() -> InvalidSurveyAnswerCodeException.of("Q5 경비관리", request.q5()));

        Question6Spend q6 = EnumUtils.fromCode(Question6Spend.class, request.q6())
                .orElseThrow(() -> InvalidSurveyAnswerCodeException.of("Q6 소비태도", request.q6()));

        List<Question7Interest> q7 = request.q7().stream()
                .map(code -> EnumUtils.fromCode(Question7Interest.class, code)
                        .orElseThrow(() -> InvalidSurveyAnswerCodeException.of("Q7 선호활동", code)))
                .collect(Collectors.toList());

        Question8Planning q8 = EnumUtils.fromCode(Question8Planning.class, request.q8())
                .orElseThrow(() -> InvalidSurveyAnswerCodeException.of("Q8 계획성", request.q8()));

        Question9Menu q9 = EnumUtils.fromCode(Question9Menu.class, request.q9())
                .orElseThrow(() -> InvalidSurveyAnswerCodeException.of("Q9 낯선메뉴", request.q9()));

        Question10Companion q10 = EnumUtils.fromCode(Question10Companion.class, request.q10())
                .orElseThrow(() -> InvalidSurveyAnswerCodeException.of("Q10 동행제안", request.q10()));

        Question11Photo q11 = EnumUtils.fromCode(Question11Photo.class, request.q11())
                .orElseThrow(() -> InvalidSurveyAnswerCodeException.of("Q11 사진", request.q11()));

        return Survey.builder()
                .user(user)
                .q1Transport(q1)
                .q2Waiting(q2)
                .q3Stay(q3)
                .q4Wakeup(q4)
                .q5Expense(q5)
                .q6Spend(q6)
                .q7Interests(q7)
                .q8Planning(q8)
                .q9Menu(q9)
                .q10Companion(q10)
                .q11Photo(q11)
                .build();
    }
}
