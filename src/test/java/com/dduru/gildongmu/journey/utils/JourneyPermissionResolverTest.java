package com.dduru.gildongmu.journey.utils;

import com.dduru.gildongmu.post.domain.Post;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.domain.enums.OauthType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("여행 수정 권한 계산 테스트")
class JourneyPermissionResolverTest {

    @Test
    @DisplayName("호스트가 아니면 수정 권한이 없다")
    void canEdit_notHost_returnsFalse() {
        LocalDate today = LocalDate.of(2026, 4, 14);
        Post post = createPost(1L, today.plusDays(2), today.plusDays(4), today.plusDays(1));

        boolean canEdit = JourneyPermissionResolver.canEdit(post, 99L, today);

        assertThat(canEdit).isFalse();
    }

    @Test
    @DisplayName("호스트이고 모집 마감 전이며 여행 시작 전이면 수정 권한이 있다")
    void canEdit_hostBeforeDeadlineAndBeforeStart_returnsTrue() {
        LocalDate today = LocalDate.of(2026, 4, 14);
        Post post = createPost(1L, today.plusDays(2), today.plusDays(4), today.plusDays(1));

        boolean canEdit = JourneyPermissionResolver.canEdit(post, 1L, today);

        assertThat(canEdit).isTrue();
    }

    @Test
    @DisplayName("호스트는 여행 시작 당일까지 수정 권한이 있다")
    void canEdit_hostOnStartDate_returnsTrue() {
        LocalDate today = LocalDate.of(2026, 4, 14);
        Post post = createPost(1L, today, today.plusDays(2), today.plusDays(1));

        boolean canEdit = JourneyPermissionResolver.canEdit(post, 1L, today);

        assertThat(canEdit).isTrue();
    }

    @Test
    @DisplayName("호스트여도 모집 마감일이 지나면 수정 권한이 없다")
    void canEdit_hostAfterDeadline_returnsFalse() {
        LocalDate today = LocalDate.of(2026, 4, 14);
        Post post = createPost(1L, today.plusDays(2), today.plusDays(4), today.minusDays(1));

        boolean canEdit = JourneyPermissionResolver.canEdit(post, 1L, today);

        assertThat(canEdit).isFalse();
    }

    @Test
    @DisplayName("호스트여도 여행 시작 다음 날부터는 수정 권한이 없다")
    void canEdit_hostTraveling_returnsFalse() {
        LocalDate today = LocalDate.of(2026, 4, 14);
        Post post = createPost(1L, today.minusDays(1), today.plusDays(1), today.plusDays(3));

        boolean canEdit = JourneyPermissionResolver.canEdit(post, 1L, today);

        assertThat(canEdit).isFalse();
    }

    @Test
    @DisplayName("호스트여도 여행 종료 후에는 수정 권한이 없다")
    void canEdit_hostCompleted_returnsFalse() {
        LocalDate today = LocalDate.of(2026, 4, 14);
        Post post = createPost(1L, today.minusDays(4), today.minusDays(1), today.plusDays(3));

        boolean canEdit = JourneyPermissionResolver.canEdit(post, 1L, today);

        assertThat(canEdit).isFalse();
    }

    private Post createPost(Long hostId, LocalDate startDate, LocalDate endDate, LocalDate recruitDeadline) {
        User host = User.builder()
                .email("host@example.com")
                .name("host")
                .oauthId("host-oauth")
                .oauthType(OauthType.KAKAO)
                .build();
        ReflectionTestUtils.setField(host, "id", hostId);

        return Post.builder()
                .user(host)
                .startDate(startDate)
                .endDate(endDate)
                .recruitDeadline(recruitDeadline)
                .recruitCapacity(3)
                .build();
    }
}
