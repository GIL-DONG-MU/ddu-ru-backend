package com.dduru.gildongmu.survey.service;

import com.dduru.gildongmu.survey.domain.Survey;
import com.dduru.gildongmu.survey.domain.enums.*;
import com.dduru.gildongmu.survey.dto.response.TendencyScoreResponse;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.domain.enums.OauthType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.withPrecision;

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
    @DisplayName("모든_축_첫번째_선택지_0점_환산")
    void 모든_축_첫번째_선택지_0점_환산() {
        Survey survey = Survey.createSurvey(
                testUser,
                RhythmQuestion1.PLANNED_ROUTE,
                RhythmQuestion2.PACK_EARLY,
                RhythmQuestion3.ROUTE_TIME_SET,
                ConsumptionQuestion1.ADJUST_BUDGET,
                ConsumptionQuestion2.VALUE_TRANSPORT,
                ConsumptionQuestion3.VALUE_CHOICE,
                EnergyQuestion1.RELAXED_DAY,
                EnergyQuestion2.BRUNCH_INSTEAD,
                EnergyQuestion3.DO_NOTHING_OK,
                DecisionQuestion1.DELEGATE_ROLE,
                DecisionQuestion2.FOLLOW_OTHERS,
                DecisionQuestion3.WAIT_AND_SEE,
                RecordStyleQuestion.EYES_FIRST,
                List.of(ActivityTag.SIGHTSEEING, ActivityTag.NATURE, ActivityTag.FOOD)
        );

        TendencyScoreResponse scores = calculator.calculate(survey);

        assertThat(scores.rhythmScore()).isEqualTo(0.0);
        assertThat(scores.energyScore()).isEqualTo(0.0);
        assertThat(scores.consumptionScore()).isEqualTo(0.0);
        assertThat(scores.decisionScore()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("모든_축_두번째_선택지_10점_환산")
    void 모든_축_두번째_선택지_10점_환산() {
        Survey survey = Survey.createSurvey(
                testUser,
                RhythmQuestion1.IMPULSE_SIDE_TRIP,
                RhythmQuestion2.PACK_LAST_MINUTE,
                RhythmQuestion3.ROUGH_LIST_ONLY,
                ConsumptionQuestion1.FLEX_OK,
                ConsumptionQuestion2.SAVE_TIME_TAXI,
                ConsumptionQuestion3.INVEST_EXPERIENCE,
                EnergyQuestion1.PACKED_DAY,
                EnergyQuestion2.BREAKFAST_SPRINT,
                EnergyQuestion3.FILL_WITH_SPOTS,
                DecisionQuestion1.LEAD_OR_ORGANIZE,
                DecisionQuestion2.PROPOSE_FIRST,
                DecisionQuestion3.DRIVE_CONCLUSION,
                RecordStyleQuestion.SHOOT_NOW,
                List.of(ActivityTag.SIGHTSEEING)
        );

        TendencyScoreResponse scores = calculator.calculate(survey);

        assertThat(scores.rhythmScore()).isEqualTo(10.0);
        assertThat(scores.energyScore()).isEqualTo(10.0);
        assertThat(scores.consumptionScore()).isEqualTo(10.0);
        assertThat(scores.decisionScore()).isEqualTo(10.0);
    }

    @Test
    @DisplayName("원점수_4점일때_10점_환산_약_6_67")
    void 원점수_4점일때_10점_환산_약_6_67() {
        Survey survey = Survey.createSurvey(
                testUser,
                RhythmQuestion1.PLANNED_ROUTE,
                RhythmQuestion2.PACK_LAST_MINUTE,
                RhythmQuestion3.ROUGH_LIST_ONLY,
                ConsumptionQuestion1.ADJUST_BUDGET,
                ConsumptionQuestion2.VALUE_TRANSPORT,
                ConsumptionQuestion3.VALUE_CHOICE,
                EnergyQuestion1.RELAXED_DAY,
                EnergyQuestion2.BRUNCH_INSTEAD,
                EnergyQuestion3.DO_NOTHING_OK,
                DecisionQuestion1.DELEGATE_ROLE,
                DecisionQuestion2.FOLLOW_OTHERS,
                DecisionQuestion3.WAIT_AND_SEE,
                RecordStyleQuestion.EYES_FIRST,
                List.of(ActivityTag.SIGHTSEEING)
        );

        TendencyScoreResponse scores = calculator.calculate(survey);

        assertThat(scores.rhythmScore()).isCloseTo(6.7, withPrecision(0.01));
        assertThat(scores.energyScore()).isEqualTo(0.0);
        assertThat(scores.consumptionScore()).isEqualTo(0.0);
        assertThat(scores.decisionScore()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("점수는_항상_0에서_10_사이")
    void 점수는_항상_0에서_10_사이() {
        Survey survey = Survey.createSurvey(
                testUser,
                RhythmQuestion1.IMPULSE_SIDE_TRIP,
                RhythmQuestion2.PACK_EARLY,
                RhythmQuestion3.ROUGH_LIST_ONLY,
                ConsumptionQuestion1.FLEX_OK,
                ConsumptionQuestion2.VALUE_TRANSPORT,
                ConsumptionQuestion3.INVEST_EXPERIENCE,
                EnergyQuestion1.PACKED_DAY,
                EnergyQuestion2.BRUNCH_INSTEAD,
                EnergyQuestion3.FILL_WITH_SPOTS,
                DecisionQuestion1.LEAD_OR_ORGANIZE,
                DecisionQuestion2.FOLLOW_OTHERS,
                DecisionQuestion3.DRIVE_CONCLUSION,
                RecordStyleQuestion.SHOOT_NOW,
                List.of(ActivityTag.FESTIVAL)
        );

        TendencyScoreResponse scores = calculator.calculate(survey);

        assertThat(scores.rhythmScore()).isBetween(0.0, 10.0);
        assertThat(scores.energyScore()).isBetween(0.0, 10.0);
        assertThat(scores.consumptionScore()).isBetween(0.0, 10.0);
        assertThat(scores.decisionScore()).isBetween(0.0, 10.0);
    }
}
