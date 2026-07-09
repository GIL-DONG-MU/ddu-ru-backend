package com.dduru.gildongmu.survey.domain;

import com.dduru.gildongmu.survey.domain.enums.AvatarType;
import com.dduru.gildongmu.survey.exception.InvalidTravelTendencyScoreException;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.domain.enums.OauthType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("TravelTendency 테스트")
class TravelTendencyTest {

    @Test
    @DisplayName("0.0 이상 10.0 이하 성향 점수로 생성할 수 있다")
    void createWithValidScores() {
        TravelTendency tendency = TravelTendency.create(
                user(),
                BigDecimal.ZERO,
                BigDecimal.valueOf(3.5),
                BigDecimal.valueOf(7.0),
                BigDecimal.TEN,
                AvatarType.TTUR_POGUN
        );

        assertThat(tendency.getRhythmScore()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(tendency.getDecisionScore()).isEqualByComparingTo(BigDecimal.TEN);
    }

    @Test
    @DisplayName("생성 시 성향 점수가 0.0 미만이면 예외가 발생한다")
    void createRejectsScoreLessThanZero() {
        assertThatThrownBy(() -> TravelTendency.create(
                user(),
                BigDecimal.valueOf(-0.1),
                BigDecimal.valueOf(5.0),
                BigDecimal.valueOf(5.0),
                BigDecimal.valueOf(5.0),
                AvatarType.TTUR_POGUN
        )).isInstanceOf(InvalidTravelTendencyScoreException.class);
    }

    @Test
    @DisplayName("생성 시 성향 점수가 10.0 초과면 예외가 발생한다")
    void createRejectsScoreGreaterThanTen() {
        assertThatThrownBy(() -> TravelTendency.create(
                user(),
                BigDecimal.valueOf(5.0),
                BigDecimal.valueOf(10.1),
                BigDecimal.valueOf(5.0),
                BigDecimal.valueOf(5.0),
                AvatarType.TTUR_POGUN
        )).isInstanceOf(InvalidTravelTendencyScoreException.class);
    }

    @Test
    @DisplayName("수정 시에도 동일한 성향 점수 검증을 적용한다")
    void updateRejectsInvalidScore() {
        TravelTendency tendency = TravelTendency.create(
                user(),
                BigDecimal.valueOf(5.0),
                BigDecimal.valueOf(5.0),
                BigDecimal.valueOf(5.0),
                BigDecimal.valueOf(5.0),
                AvatarType.TTUR_POGUN
        );

        assertThatThrownBy(() -> tendency.update(
                BigDecimal.valueOf(5.0),
                BigDecimal.valueOf(5.0),
                BigDecimal.valueOf(99.9),
                BigDecimal.valueOf(5.0),
                AvatarType.TTUR_DASOM
        )).isInstanceOf(InvalidTravelTendencyScoreException.class);
    }

    @Test
    @DisplayName("성향 점수가 null이면 예외가 발생한다")
    void rejectsNullScore() {
        assertThatThrownBy(() -> TravelTendency.create(
                user(),
                null,
                BigDecimal.valueOf(5.0),
                BigDecimal.valueOf(5.0),
                BigDecimal.valueOf(5.0),
                AvatarType.TTUR_POGUN
        )).isInstanceOf(InvalidTravelTendencyScoreException.class);
    }

    @Test
    @DisplayName("성향 점수 범위를 제공한다")
    void scoreRange() {
        assertThat(TravelTendency.scoreRange()).isEqualTo(10.0);
    }

    private User user() {
        return User.builder()
                .email("travel-tendency@example.com")
                .name("성향테스트")
                .oauthId("travel-tendency-oauth")
                .oauthType(OauthType.KAKAO)
                .build();
    }
}
