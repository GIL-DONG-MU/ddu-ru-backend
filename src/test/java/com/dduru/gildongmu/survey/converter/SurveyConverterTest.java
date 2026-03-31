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
        // given
        SurveyRequest request = new SurveyRequest(
                1, 1, 1, 1, 1, 2,
                List.of(1, 2, 3),
                2, 1, 2, 1
        );

        // when
        Survey survey = converter.toEntity(testUser, request);

        // then
        assertThat(survey.getUser()).isEqualTo(testUser);
        assertThat(survey.getQ1Transport()).isEqualTo(Question1Transport.WALK_BUS);
        assertThat(survey.getQ2Waiting()).isEqualTo(Question2Waiting.WAIT);
        assertThat(survey.getQ3Stay()).isEqualTo(Question3Stay.HOTEL);
        assertThat(survey.getQ4Wakeup()).isEqualTo(Question4Wakeup.EARLY);
        assertThat(survey.getQ5Expense()).isEqualTo(Question5Expense.EACH_PAYS);
        assertThat(survey.getQ6Spend()).isEqualTo(Question6Spend.SAVE);
        assertThat(survey.getQ7Interests()).hasSize(3);
        assertThat(survey.getQ7Interests()).containsExactly(
                Question7Interest.SIGHTSEEING,
                Question7Interest.EXHIBITION,
                Question7Interest.NATURE
        );
        assertThat(survey.getQ8Planning()).isEqualTo(Question8Planning.FLEXIBLE);
        assertThat(survey.getQ9Menu()).isEqualTo(Question9Menu.SAFE);
        assertThat(survey.getQ10Companion()).isEqualTo(Question10Companion.SITUATIONAL);
        assertThat(survey.getQ11Photo()).isEqualTo(Question11Photo.LIFETIME_SHOT);
    }

    @Test
    @DisplayName("유효하지_않은_Q1_코드_예외발생")
    void 유효하지_않은_Q1_코드_예외발생() {
        // given
        SurveyRequest request = new SurveyRequest(
                999, 1, 1, 1, 1, 1, List.of(1, 2, 3), 1, 1, 1, 1
        );

        // when & then
        assertThatThrownBy(() -> converter.toEntity(testUser, request))
                .isInstanceOf(InvalidSurveyAnswerCodeException.class)
                .hasMessageContaining("잘못된 입력");
    }

    @Test
    @DisplayName("유효하지_않은_Q7_코드_예외발생")
    void 유효하지_않은_Q7_코드_예외발생() {
        // given
        SurveyRequest request = new SurveyRequest(
                1, 1, 1, 1, 1, 1,
                List.of(1, 2, 999),
                1, 1, 1, 1
        );

        // when & then
        assertThatThrownBy(() -> converter.toEntity(testUser, request))
                .isInstanceOf(InvalidSurveyAnswerCodeException.class)
                .hasMessageContaining("잘못된 입력");
    }

    @Test
    @DisplayName("모든_질문_최대값_코드_정상변환")
    void 모든_질문_최대값_코드_정상변환() {
        // given
        SurveyRequest request = new SurveyRequest(
                2, 2, 2, 2, 2, 1,
                List.of(6, 7, 8),  // Question7Interest 최대 코드는 8 (FESTIVAL)
                3, 3, 1, 3
        );

        // when
        Survey survey = converter.toEntity(testUser, request);

        // then
        assertThat(survey.getQ1Transport()).isEqualTo(Question1Transport.TAXI);
        assertThat(survey.getQ2Waiting()).isEqualTo(Question2Waiting.MOVE_ELSEWHERE);
        assertThat(survey.getQ3Stay()).isEqualTo(Question3Stay.JUST_SLEEP);
        assertThat(survey.getQ4Wakeup()).isEqualTo(Question4Wakeup.RELAXED);
        assertThat(survey.getQ5Expense()).isEqualTo(Question5Expense.POOLED);
        assertThat(survey.getQ6Spend()).isEqualTo(Question6Spend.SPLURGE);
        assertThat(survey.getQ7Interests()).hasSize(3);
        assertThat(survey.getQ7Interests()).containsExactly(
                Question7Interest.RESORT,
                Question7Interest.ACTIVITY,
                Question7Interest.FESTIVAL
        );
        assertThat(survey.getQ8Planning()).isEqualTo(Question8Planning.ON_SITE);
        assertThat(survey.getQ9Menu()).isEqualTo(Question9Menu.CHALLENGE);
        assertThat(survey.getQ10Companion()).isEqualTo(Question10Companion.WELCOME);
        assertThat(survey.getQ11Photo()).isEqualTo(Question11Photo.EYES_ONLY);
    }
}
