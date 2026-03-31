package com.dduru.gildongmu.survey.converter;

import com.dduru.gildongmu.common.enums.CodedEnum;
import com.dduru.gildongmu.common.enums.EnumUtils;
import com.dduru.gildongmu.survey.domain.Survey;
import com.dduru.gildongmu.survey.domain.enums.*;
import com.dduru.gildongmu.survey.dto.request.SurveyRequest;
import com.dduru.gildongmu.survey.exception.InvalidSurveyAnswerCodeException;
import com.dduru.gildongmu.user.domain.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class SurveyConverter {

    public Survey toEntity(User user, SurveyRequest request) {
        ParsedSurveyData parsed = parseRequest(request);
        return Survey.createSurvey(user, parsed.q1(), parsed.q2(), parsed.q3(), parsed.q4(), parsed.q5(),
                parsed.q6(), parsed.q7(), parsed.q8(), parsed.q9(), parsed.q10(), parsed.q11());
    }

    public ParsedSurveyData parseRequest(SurveyRequest request) {
        Question1Transport q1 = toEnum(Question1Transport.class, request.q1(), "Q1 이동수단");
        Question2Waiting q2 = toEnum(Question2Waiting.class, request.q2(), "Q2 웨이팅");
        Question3Stay q3 = toEnum(Question3Stay.class, request.q3(), "Q3 숙소");
        Question4Wakeup q4 = toEnum(Question4Wakeup.class, request.q4(), "Q4 기상시간");
        Question5Expense q5 = toEnum(Question5Expense.class, request.q5(), "Q5 경비관리");
        Question6Spend q6 = toEnum(Question6Spend.class, request.q6(), "Q6 소비태도");
        List<Question7Interest> q7 = toEnumList(Question7Interest.class, request.q7(), "Q7 선호활동");
        Question8Planning q8 = toEnum(Question8Planning.class, request.q8(), "Q8 계획성");
        Question9Menu q9 = toEnum(Question9Menu.class, request.q9(), "Q9 낯선메뉴");
        Question10Companion q10 = toEnum(Question10Companion.class, request.q10(), "Q10 동행제안");
        Question11Photo q11 = toEnum(Question11Photo.class, request.q11(), "Q11 사진");

        return new ParsedSurveyData(q1, q2, q3, q4, q5, q6, q7, q8, q9, q10, q11);
    }

    private <E extends Enum<E> & CodedEnum> E toEnum(
            Class<E> enumClass, Integer code, String questionName) {
        return EnumUtils.fromCode(enumClass, code)
                .orElseThrow(InvalidSurveyAnswerCodeException::new);
    }

    private <E extends Enum<E> & CodedEnum> List<E> toEnumList(
            Class<E> enumClass, List<Integer> codes, String questionName) {
        return codes.stream()
                .map(code -> toEnum(enumClass, code, questionName))
                .collect(Collectors.toList());
    }

    public record ParsedSurveyData(
            Question1Transport q1,
            Question2Waiting q2,
            Question3Stay q3,
            Question4Wakeup q4,
            Question5Expense q5,
            Question6Spend q6,
            List<Question7Interest> q7,
            Question8Planning q8,
            Question9Menu q9,
            Question10Companion q10,
            Question11Photo q11
    ) {
    }
}
