package com.dduru.gildongmu.recommendation.domain;

import com.dduru.gildongmu.common.entity.BaseTimeEntity;
import com.dduru.gildongmu.recommendation.domain.enums.MateRecommendationBatchStatus;
import com.dduru.gildongmu.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(
        name = "mate_recommendation_batches",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_mate_recommendation_batches_user_date",
                columnNames = {"user_id", "recommendation_date"}
        ),
        indexes = @Index(
                name = "idx_mate_recommendation_batches_date_status",
                columnList = "recommendation_date,status"
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MateRecommendationBatch extends BaseTimeEntity {

    private static final int MAX_FAILURE_REASON_LENGTH = 500;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "recommendation_date", nullable = false)
    private LocalDate recommendationDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MateRecommendationBatchStatus status;

    @Column(name = "failure_reason", length = MAX_FAILURE_REASON_LENGTH)
    private String failureReason;

    private MateRecommendationBatch(User user, LocalDate recommendationDate) {
        this.user = user;
        this.recommendationDate = recommendationDate;
        this.status = MateRecommendationBatchStatus.CREATED;
    }

    public static MateRecommendationBatch create(User user, LocalDate recommendationDate) {
        return new MateRecommendationBatch(user, recommendationDate);
    }

    public void complete() {
        requireStatus(MateRecommendationBatchStatus.CREATED);
        status = MateRecommendationBatchStatus.COMPLETED;
        failureReason = null;
    }

    public void markEmpty() {
        requireStatus(MateRecommendationBatchStatus.CREATED);
        status = MateRecommendationBatchStatus.EMPTY;
        failureReason = null;
    }

    public void fail(String reason) {
        requireStatus(MateRecommendationBatchStatus.CREATED);
        status = MateRecommendationBatchStatus.FAILED;
        failureReason = truncate(reason);
    }

    public void retry() {
        requireStatus(MateRecommendationBatchStatus.FAILED);
        status = MateRecommendationBatchStatus.CREATED;
        failureReason = null;
    }

    private void requireStatus(MateRecommendationBatchStatus expected) {
        if (status != expected) {
            throw new IllegalStateException("Invalid recommendation batch transition: status=" + status);
        }
    }

    private String truncate(String value) {
        if (value == null) {
            return null;
        }
        return value.length() <= MAX_FAILURE_REASON_LENGTH
                ? value
                : value.substring(0, MAX_FAILURE_REASON_LENGTH);
    }
}
