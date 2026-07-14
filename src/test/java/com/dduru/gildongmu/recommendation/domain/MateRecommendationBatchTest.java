package com.dduru.gildongmu.recommendation.domain;

import com.dduru.gildongmu.recommendation.domain.enums.MateRecommendationBatchStatus;
import com.dduru.gildongmu.user.domain.User;
import com.dduru.gildongmu.user.domain.enums.OauthType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("MateRecommendationBatch 테스트")
class MateRecommendationBatchTest {

    @Test
    @DisplayName("CREATED 배치는 COMPLETED와 EMPTY로 전환할 수 있다")
    void completesOrBecomesEmpty() {
        MateRecommendationBatch completed = batch();
        MateRecommendationBatch empty = batch();

        completed.complete();
        empty.markEmpty();

        assertThat(completed.getStatus()).isEqualTo(MateRecommendationBatchStatus.COMPLETED);
        assertThat(empty.getStatus()).isEqualTo(MateRecommendationBatchStatus.EMPTY);
    }

    @Test
    @DisplayName("FAILED 배치만 CREATED로 재시도할 수 있다")
    void retriesOnlyFailedBatch() {
        MateRecommendationBatch failed = batch();
        failed.fail("temporary failure");

        failed.retry();

        assertThat(failed.getStatus()).isEqualTo(MateRecommendationBatchStatus.CREATED);
        assertThat(failed.getFailureReason()).isNull();
        assertThatThrownBy(failed::retry).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("실패 사유는 DB 컬럼 길이인 500자로 제한한다")
    void truncatesFailureReason() {
        MateRecommendationBatch batch = batch();

        batch.fail("x".repeat(600));

        assertThat(batch.getFailureReason()).hasSize(500);
    }

    private MateRecommendationBatch batch() {
        User user = User.builder()
                .email("batch@example.com")
                .name("batch")
                .oauthId("batch-oauth")
                .oauthType(OauthType.KAKAO)
                .build();
        return MateRecommendationBatch.create(user, LocalDate.of(2026, 7, 15));
    }
}
