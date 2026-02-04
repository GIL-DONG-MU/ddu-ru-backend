package com.dduru.gildongmu.survey.service;

import com.dduru.gildongmu.survey.domain.Survey;
import com.dduru.gildongmu.survey.domain.enums.*;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.domain.enums.OauthType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("여행 성향 점수 계산 테스트")
class TravelTendencyCalculatorTest {

    private TravelTendencyCalculator calculator;
    private User testUser;

    @BeforeEach
    void setUp() {
        calculator = new TravelTendencyCalculator();
        testUser = User.builder()
                .email("test@example.com")
                .name("테스트")
                .oauthId("12345")
                .oauthType(OauthType.KAKAO)
                .build();
    }

    @Test
    @DisplayName("기본값_모두_5점으로_시작")
    void 기본값_모두_5점으로_시작() {
        // given
        Survey survey = Survey.createSurvey(
                testUser,
                Question1Transport.WALK_BUS,
                Question2Waiting.WAIT,
                Question3Stay.HOTEL,
                Question4Wakeup.EARLY,
                Question5Expense.EACH_PAYS,
                Question6Spend.SAVE,
                List.of(Question7Interest.SIGHTSEEING, Question7Interest.NATURE, Question7Interest.RESORT),
                Question8Planning.FLEXIBLE,
                Question9Menu.SAFE,
                Question10Companion.SITUATIONAL,
                Question11Photo.EYES_ONLY
        );

        // when
        TravelTendencyCalculator.TendencyScores scores = calculator.calculate(survey);

        // then
        assertThat(scores.r()).isBetween(0.0, 10.0);
        assertThat(scores.w()).isBetween(0.0, 10.0);
        assertThat(scores.s()).isBetween(0.0, 10.0);
        assertThat(scores.p()).isBetween(0.0, 10.0);
    }

    @Test
    @DisplayName("Q1_걷기버스선택시_W감소_P증가")
    void Q1_걷기버스선택시_W감소_P증가() {
        // given
        Survey walkBusSurvey = Survey.createSurvey(
                testUser, Question1Transport.WALK_BUS,
                Question2Waiting.WAIT, Question3Stay.HOTEL, Question4Wakeup.EARLY,
                Question5Expense.EACH_PAYS, Question6Spend.SAVE,
                List.of(Question7Interest.SIGHTSEEING, Question7Interest.NATURE, Question7Interest.RESORT),
                Question8Planning.FLEXIBLE, Question9Menu.SAFE,
                Question10Companion.SITUATIONAL, Question11Photo.EYES_ONLY
        );

        Survey taxiSurvey = Survey.createSurvey(
                testUser, Question1Transport.TAXI,
                Question2Waiting.WAIT, Question3Stay.HOTEL, Question4Wakeup.EARLY,
                Question5Expense.EACH_PAYS, Question6Spend.SAVE,
                List.of(Question7Interest.SIGHTSEEING, Question7Interest.NATURE, Question7Interest.RESORT),
                Question8Planning.FLEXIBLE, Question9Menu.SAFE,
                Question10Companion.SITUATIONAL, Question11Photo.EYES_ONLY
        );

        // when
        TravelTendencyCalculator.TendencyScores walkBusScores = calculator.calculate(walkBusSurvey);
        TravelTendencyCalculator.TendencyScores taxiScores = calculator.calculate(taxiSurvey);

        // then
        assertThat(walkBusScores.w()).isLessThan(taxiScores.w());
        assertThat(walkBusScores.p()).isGreaterThan(taxiScores.p());
    }

    @Test
    @DisplayName("Q7_선호활동_택3_누적합산_정상작동")
    void Q7_선호활동_택3_누적합산_정상작동() {
        // given
        Survey survey = Survey.createSurvey(
                testUser,
                Question1Transport.WALK_BUS,
                Question2Waiting.WAIT,
                Question3Stay.JUST_SLEEP,
                Question4Wakeup.EARLY,
                Question5Expense.POOLED,
                Question6Spend.SPLURGE,
                List.of(Question7Interest.FOOD, Question7Interest.SHOPPING, Question7Interest.ACTIVITY),
                Question8Planning.FLEXIBLE,
                Question9Menu.CHECK_REVIEW,
                Question10Companion.SITUATIONAL,
                Question11Photo.MATCH_COMPANION
        );

        // when
        TravelTendencyCalculator.TendencyScores scores = calculator.calculate(survey);

        // then
        assertThat(scores.r()).isGreaterThan(5.0);
        assertThat(scores.p()).isGreaterThan(5.0);
    }

    @Test
    @DisplayName("점수_범위_0점에서_10점으로_제한")
    void 점수_범위_0점에서_10점으로_제한() {
        // given
        Survey minScoreSurvey = Survey.createSurvey(
                testUser,
                Question1Transport.WALK_BUS,
                Question2Waiting.MOVE_ELSEWHERE,
                Question3Stay.JUST_SLEEP,
                Question4Wakeup.RELAXED,
                Question5Expense.EACH_PAYS,
                Question6Spend.SAVE,
                List.of(Question7Interest.EXHIBITION, Question7Interest.NATURE, Question7Interest.RESORT),
                Question8Planning.DETAILED,
                Question9Menu.SAFE,
                Question10Companion.US_ONLY,
                Question11Photo.EYES_ONLY
        );

        // when
        TravelTendencyCalculator.TendencyScores scores = calculator.calculate(minScoreSurvey);

        // then
        assertThat(scores.r()).isGreaterThanOrEqualTo(0.0);
        assertThat(scores.w()).isGreaterThanOrEqualTo(0.0);
        assertThat(scores.s()).isGreaterThanOrEqualTo(0.0);
        assertThat(scores.p()).isGreaterThanOrEqualTo(0.0);
        assertThat(scores.r()).isLessThanOrEqualTo(10.0);
        assertThat(scores.w()).isLessThanOrEqualTo(10.0);
        assertThat(scores.s()).isLessThanOrEqualTo(10.0);
        assertThat(scores.p()).isLessThanOrEqualTo(10.0);
    }

    @Test
    @DisplayName("Q10_완전환영선택시_S크게증가")
    void Q10_완전환영선택시_S크게증가() {
        // given
        Survey welcomeSurvey = Survey.createSurvey(
                testUser,
                Question1Transport.WALK_BUS, Question2Waiting.WAIT, Question3Stay.HOTEL,
                Question4Wakeup.EARLY, Question5Expense.EACH_PAYS, Question6Spend.SAVE,
                List.of(Question7Interest.SIGHTSEEING, Question7Interest.NATURE, Question7Interest.RESORT),
                Question8Planning.FLEXIBLE, Question9Menu.SAFE,
                Question10Companion.WELCOME,
                Question11Photo.EYES_ONLY
        );

        Survey onlyUsSurvey = Survey.createSurvey(
                testUser,
                Question1Transport.WALK_BUS, Question2Waiting.WAIT, Question3Stay.HOTEL,
                Question4Wakeup.EARLY, Question5Expense.EACH_PAYS, Question6Spend.SAVE,
                List.of(Question7Interest.SIGHTSEEING, Question7Interest.NATURE, Question7Interest.RESORT),
                Question8Planning.FLEXIBLE, Question9Menu.SAFE,
                Question10Companion.US_ONLY,
                Question11Photo.EYES_ONLY
        );

        // when
        TravelTendencyCalculator.TendencyScores welcomeScores = calculator.calculate(welcomeSurvey);
        TravelTendencyCalculator.TendencyScores onlyUsScores = calculator.calculate(onlyUsSurvey);

        // then
        assertThat(welcomeScores.s()).isGreaterThan(onlyUsScores.s() + 4.0);
    }
}
