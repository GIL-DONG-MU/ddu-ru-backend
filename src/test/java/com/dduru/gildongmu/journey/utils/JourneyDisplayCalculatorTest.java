package com.dduru.gildongmu.journey.utils;

import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.domain.enums.OauthType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("여행 표시 정보 계산 테스트")
class JourneyDisplayCalculatorTest {

    @Test
    @DisplayName("시작일과 종료일이 같으면 당일 일정 텍스트를 반환한다")
    void tripDurationText_sameDay_returnsOneDayText() {
        Post post = createPost(LocalDate.of(2026, 4, 14), LocalDate.of(2026, 4, 14), null);

        String result = JourneyDisplayCalculator.tripDurationText(post);

        assertThat(result).isEqualTo("당일 일정");
    }

    @Test
    @DisplayName("2박 3일 일정은 박/일 텍스트를 반환한다")
    void tripDurationText_multiDay_returnsNightDayText() {
        Post post = createPost(LocalDate.of(2026, 4, 14), LocalDate.of(2026, 4, 16), null);

        String result = JourneyDisplayCalculator.tripDurationText(post);

        assertThat(result).isEqualTo("2박 3일");
    }

    @Test
    @DisplayName("마감일이 없으면 상시 모집을 반환한다")
    void recruitDeadlineDDay_noDeadline_returnsAlwaysOpen() {
        Post post = createPost(LocalDate.of(2026, 4, 14), LocalDate.of(2026, 4, 16), null);

        String result = JourneyDisplayCalculator.recruitDeadlineDDay(post, LocalDate.of(2026, 4, 14));

        assertThat(result).isEqualTo("상시 모집");
    }

    @Test
    @DisplayName("오늘이 마감일이면 D-Day를 반환한다")
    void recruitDeadlineDDay_sameDay_returnsDDay() {
        LocalDate today = LocalDate.of(2026, 4, 14);
        Post post = createPost(today.plusDays(1), today.plusDays(2), today);

        String result = JourneyDisplayCalculator.recruitDeadlineDDay(post, today);

        assertThat(result).isEqualTo("D-Day");
    }

    @Test
    @DisplayName("마감일 전이면 D-n을 반환한다")
    void recruitDeadlineDDay_beforeDeadline_returnsDMinusN() {
        LocalDate today = LocalDate.of(2026, 4, 14);
        Post post = createPost(today.plusDays(1), today.plusDays(2), today.plusDays(2));

        String result = JourneyDisplayCalculator.recruitDeadlineDDay(post, today);

        assertThat(result).isEqualTo("D-2");
    }

    @Test
    @DisplayName("마감일이 지났으면 마감을 반환한다")
    void recruitDeadlineDDay_afterDeadline_returnsClosed() {
        LocalDate today = LocalDate.of(2026, 4, 14);
        Post post = createPost(today.plusDays(1), today.plusDays(2), today.minusDays(1));

        String result = JourneyDisplayCalculator.recruitDeadlineDDay(post, today);

        assertThat(result).isEqualTo("마감");
    }

    private Post createPost(LocalDate startDate, LocalDate endDate, LocalDate recruitDeadline) {
        User author = User.builder()
                .email("author@example.com")
                .name("author")
                .oauthId("author-oauth")
                .oauthType(OauthType.KAKAO)
                .build();
        ReflectionTestUtils.setField(author, "id", 1L);

        Post post = Post.builder()
                .user(author)
                .startDate(startDate)
                .endDate(endDate)
                .recruitCapacity(3)
                .build();
        ReflectionTestUtils.setField(post, "recruitDeadline", recruitDeadline);
        return post;
    }
}
