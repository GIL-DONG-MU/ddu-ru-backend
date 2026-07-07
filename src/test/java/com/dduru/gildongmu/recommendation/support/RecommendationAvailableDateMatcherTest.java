package com.dduru.gildongmu.recommendation.support;

import com.dduru.gildongmu.post.domain.enums.CompanionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("RecommendationAvailableDateMatcher 테스트")
class RecommendationAvailableDateMatcherTest {

    private final RecommendationAvailableDateMatcher matcher = new RecommendationAvailableDateMatcher();

    @Test
    @DisplayName("가능 기간이 없으면 날짜 필터를 적용하지 않는다")
    void matchesWhenNoAvailableDates() {
        boolean result = matcher.matches(
                CompanionType.FULL,
                LocalDate.of(2026, 7, 1),
                LocalDate.of(2026, 7, 3),
                List.of()
        );

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("FULL은 사용자 가능 기간이 여행방 전체 일정을 포함해야 한다")
    void fullRequiresContainingWholeTrip() {
        AvailableDateRange range = range(2026, 7, 1, 2026, 7, 3);

        assertThat(matcher.matches(CompanionType.FULL, date(2026, 7, 1), date(2026, 7, 3), List.of(range))).isTrue();
        assertThat(matcher.matches(CompanionType.FULL, date(2026, 7, 1), date(2026, 7, 4), List.of(range))).isFalse();
    }

    @Test
    @DisplayName("PARTIAL은 양 끝 포함 겹치는 날짜가 2일 이상이어야 한다")
    void partialRequiresAtLeastTwoOverlappedDays() {
        AvailableDateRange range = range(2026, 7, 1, 2026, 7, 3);

        assertThat(matcher.matches(CompanionType.PARTIAL, date(2026, 7, 3), date(2026, 7, 4), List.of(range))).isFalse();
        assertThat(matcher.matches(CompanionType.PARTIAL, date(2026, 7, 2), date(2026, 7, 4), List.of(range))).isTrue();
    }

    @Test
    @DisplayName("MEAL은 양 끝 포함 겹치는 날짜가 1일 이상이면 통과한다")
    void mealRequiresAtLeastOneOverlappedDay() {
        AvailableDateRange range = range(2026, 7, 1, 2026, 7, 3);

        assertThat(matcher.matches(CompanionType.MEAL, date(2026, 7, 4), date(2026, 7, 5), List.of(range))).isFalse();
        assertThat(matcher.matches(CompanionType.MEAL, date(2026, 7, 3), date(2026, 7, 5), List.of(range))).isTrue();
    }

    @Test
    @DisplayName("UNSPECIFIED는 양 끝 포함 겹치는 날짜가 1일 이상이면 통과한다")
    void unspecifiedCompanionTypeRequiresAtLeastOneOverlappedDay() {
        AvailableDateRange range = range(2026, 7, 1, 2026, 7, 3);

        assertThat(matcher.matches(CompanionType.UNSPECIFIED, date(2026, 7, 4), date(2026, 7, 5), List.of(range))).isFalse();
        assertThat(matcher.matches(CompanionType.UNSPECIFIED, date(2026, 7, 3), date(2026, 7, 5), List.of(range))).isTrue();
    }

    private AvailableDateRange range(int startYear, int startMonth, int startDay, int endYear, int endMonth, int endDay) {
        return new AvailableDateRange(
                LocalDate.of(startYear, startMonth, startDay),
                LocalDate.of(endYear, endMonth, endDay)
        );
    }

    private LocalDate date(int year, int month, int day) {
        return LocalDate.of(year, month, day);
    }
}
