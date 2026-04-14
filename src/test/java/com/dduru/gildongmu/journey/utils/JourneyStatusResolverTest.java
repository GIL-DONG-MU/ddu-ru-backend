package com.dduru.gildongmu.journey.utils;

import com.dduru.gildongmu.journey.domain.enums.JourneyStatus;
import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.post.domain.enums.PostStatus;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.domain.enums.OauthType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("여행 단계 계산 테스트")
class JourneyStatusResolverTest {

    @Test
    @DisplayName("여행 시작 전이고 OPEN이며 정원이 남아 있으면 RECRUITING 이다")
    void resolve_beforeStart_openAndNotFull_returnsRecruiting() {
        LocalDate today = LocalDate.of(2026, 4, 14);
        Post post = createPost(today.plusDays(2), today.plusDays(4), 3);

        JourneyStatus status = JourneyStatusResolver.resolve(post, today);

        assertThat(status).isEqualTo(JourneyStatus.RECRUITING);
    }

    @Test
    @DisplayName("여행 시작 전이라도 CLOSED 이면 RECRUITMENT_CLOSED 이다")
    void resolve_beforeStart_closedStatus_returnsRecruitmentClosed() {
        LocalDate today = LocalDate.of(2026, 4, 14);
        Post post = createPost(today.plusDays(2), today.plusDays(4), 3);
        ReflectionTestUtils.setField(post, "status", PostStatus.CLOSED);

        JourneyStatus status = JourneyStatusResolver.resolve(post, today);

        assertThat(status).isEqualTo(JourneyStatus.RECRUITMENT_CLOSED);
    }

    @Test
    @DisplayName("여행 시작 전이라도 정원이 가득 찼으면 RECRUITMENT_CLOSED 이다")
    void resolve_beforeStart_fullPost_returnsRecruitmentClosed() {
        LocalDate today = LocalDate.of(2026, 4, 14);
        Post post = createPost(today.plusDays(2), today.plusDays(4), 1);

        JourneyStatus status = JourneyStatusResolver.resolve(post, today);

        assertThat(status).isEqualTo(JourneyStatus.RECRUITMENT_CLOSED);
    }

    @Test
    @DisplayName("시작일과 종료일 사이는 TRAVELING 이다")
    void resolve_betweenStartAndEnd_returnsTraveling() {
        LocalDate today = LocalDate.of(2026, 4, 14);
        Post post = createPost(today.minusDays(1), today.plusDays(1), 3);

        JourneyStatus status = JourneyStatusResolver.resolve(post, today);

        assertThat(status).isEqualTo(JourneyStatus.TRAVELING);
    }

    @Test
    @DisplayName("종료일 다음 날부터 COMPLETED 이다")
    void resolve_afterEnd_returnsCompleted() {
        LocalDate today = LocalDate.of(2026, 4, 14);
        Post post = createPost(today.minusDays(4), today.minusDays(1), 3);

        JourneyStatus status = JourneyStatusResolver.resolve(post, today);

        assertThat(status).isEqualTo(JourneyStatus.COMPLETED);
    }

    private Post createPost(LocalDate startDate, LocalDate endDate, int recruitCapacity) {
        User author = User.builder()
                .email("author@example.com")
                .name("author")
                .oauthId("author-oauth")
                .oauthType(OauthType.KAKAO)
                .build();
        ReflectionTestUtils.setField(author, "id", 1L);

        return Post.builder()
                .user(author)
                .startDate(startDate)
                .endDate(endDate)
                .recruitCapacity(recruitCapacity)
                .build();
    }
}
