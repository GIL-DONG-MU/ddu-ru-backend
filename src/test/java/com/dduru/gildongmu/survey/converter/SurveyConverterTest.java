package com.dduru.gildongmu.survey.converter;

import com.dduru.gildongmu.survey.domain.Survey;
import com.dduru.gildongmu.survey.domain.enums.*;
import com.dduru.gildongmu.survey.dto.request.SurveyRequest;
import com.dduru.gildongmu.survey.exception.InvalidSurveyAnswerCodeException;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.domain.enums.OauthType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("설문조사 Converter 테스트")
class SurveyConverterTest {

    private SurveyConverter converter;
    private User testUser;

    @BeforeEach
    void setUp() {
        converter = new SurveyConverter();
        testUser = User.builder()
                .email("test@example.com")
                .name("테스트")
                .oauthId("12345")
                .oauthType(OauthType.KAKAO)
                .build();
    }

    @Test
    @DisplayName("유효한_코드로_Survey_엔티티_생성_성공")
    void 유효한_코드로_Survey_엔티티_생성_성공() {
        SurveyRequest request = new SurveyRequest(
                1, 1, 1,
                List.of(1, 2, 3),
                1, 1, 1,
                1, 1, 1,
                1, 1, 1,
                1
        );

        ParsedSurveyData parsed = converter.parseRequest(request);
        Survey survey = converter.toEntity(testUser, parsed);

        assertThat(survey.getUser()).isEqualTo(testUser);
        assertThat(survey.getRhythmQ1()).isEqualTo(RhythmQuestion1.PLANNED_ROUTE);
        assertThat(survey.getRhythmQ2()).isEqualTo(RhythmQuestion2.PACK_EARLY);
        assertThat(survey.getRhythmQ3()).isEqualTo(RhythmQuestion3.ROUTE_TIME_SET);
        assertThat(survey.getActivityTags()).containsExactly(
                ActivityTag.SIGHTSEEING,
                ActivityTag.EXHIBITION,
                ActivityTag.NATURE
        );
        assertThat(survey.getConsumptionQ1()).isEqualTo(ConsumptionQuestion1.ADJUST_BUDGET);
        assertThat(survey.getEnergyQ1()).isEqualTo(EnergyQuestion1.RELAXED_DAY);
        assertThat(survey.getDecisionQ1()).isEqualTo(DecisionQuestion1.DELEGATE_ROLE);
        assertThat(survey.getRecordStyle()).isEqualTo(RecordStyleQuestion.EYES_FIRST);
        assertThat(survey.getRecordStyle().getStyleType()).isEqualTo(RecordStyleType.A);
    }

    @Test
    @DisplayName("유효하지_않은_rhythmQ1_코드_예외발생")
    void 유효하지_않은_rhythmQ1_코드_예외발생() {
        SurveyRequest request = new SurveyRequest(
                999, 1, 1,
                List.of(1, 2, 3),
                1, 1, 1,
                1, 1, 1,
                1, 1, 1,
                1
        );

        assertThatThrownBy(() -> converter.parseRequest(request))
                .isInstanceOf(InvalidSurveyAnswerCodeException.class)
                .hasMessageContaining("잘못된 입력");
    }

    @Test
    @DisplayName("유효하지_않은_activityTags_코드_예외발생")
    void 유효하지_않은_activityTags_코드_예외발생() {
        SurveyRequest request = new SurveyRequest(
                1, 1, 1,
                List.of(1, 2, 999),
                1, 1, 1,
                1, 1, 1,
                1, 1, 1,
                1
        );

        assertThatThrownBy(() -> converter.parseRequest(request))
                .isInstanceOf(InvalidSurveyAnswerCodeException.class)
                .hasMessageContaining("잘못된 입력");
    }

    @Test
    @DisplayName("모든_질문_코드2_정상변환")
    void 모든_질문_코드2_정상변환() {
        SurveyRequest request = new SurveyRequest(
                2, 2, 2,
                List.of(7, 8, 9),
                2, 2, 2,
                2, 2, 2,
                2, 2, 2,
                2
        );

        ParsedSurveyData parsed = converter.parseRequest(request);
        Survey survey = converter.toEntity(testUser, parsed);

        assertThat(survey.getRhythmQ1()).isEqualTo(RhythmQuestion1.IMPULSE_SIDE_TRIP);
        assertThat(survey.getActivityTags()).containsExactly(
                ActivityTag.ACTIVITY,
                ActivityTag.AMUSEMENT_PARK,
                ActivityTag.FESTIVAL
        );
        assertThat(survey.getRecordStyle()).isEqualTo(RecordStyleQuestion.SHOOT_NOW);
        assertThat(survey.getRecordStyle().getStyleType()).isEqualTo(RecordStyleType.B);
    }
}
