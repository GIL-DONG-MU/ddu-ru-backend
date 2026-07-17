package com.dduru.gildongmu.recommendation.domain;

import com.dduru.gildongmu.recommendation.exception.InvalidAvailableDateException;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.domain.enums.OauthType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("UserRecommendationAvailableDate 테스트")
class UserRecommendationAvailableDateTest {

    @Test
    @DisplayName("시작일과 종료일이 같아도 생성할 수 있다")
    void canCreateSameDayRange() {
        LocalDate date = LocalDate.of(2026, 8, 1);

        UserRecommendationAvailableDate availableDate =
                UserRecommendationAvailableDate.of(user(), date, date);

        assertThat(availableDate.getStartDate()).isEqualTo(date);
        assertThat(availableDate.getEndDate()).isEqualTo(date);
    }

    @Test
    @DisplayName("시작일이 종료일보다 뒤면 생성할 수 없다")
    void cannotCreateWhenStartDateIsAfterEndDate() {
        assertThatThrownBy(() -> UserRecommendationAvailableDate.of(
                user(),
                LocalDate.of(2026, 8, 2),
                LocalDate.of(2026, 8, 1)
        )).isInstanceOf(InvalidAvailableDateException.class);
    }

    @Test
    @DisplayName("시작일 또는 종료일이 없으면 생성할 수 없다")
    void cannotCreateWithoutDate() {
        LocalDate date = LocalDate.of(2026, 8, 1);

        assertThatThrownBy(() -> UserRecommendationAvailableDate.of(user(), null, date))
                .isInstanceOf(InvalidAvailableDateException.class);
        assertThatThrownBy(() -> UserRecommendationAvailableDate.of(user(), date, null))
                .isInstanceOf(InvalidAvailableDateException.class);
    }

    private User user() {
        return User.builder()
                .email("available-date@example.com")
                .name("available-date")
                .oauthId("available-date-oauth")
                .oauthType(OauthType.KAKAO)
                .build();
    }
}
