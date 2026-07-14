package com.dduru.gildongmu.recommendation.domain;

import com.dduru.gildongmu.common.entity.BaseTimeEntity;
import com.dduru.gildongmu.post.domain.Post;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "mate_recommendations",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_mate_recommendations_batch_rank", columnNames = {"batch_id", "recommendation_rank"}),
                @UniqueConstraint(name = "uk_mate_recommendations_batch_post", columnNames = {"batch_id", "post_id"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MateRecommendation extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    private MateRecommendationBatch batch;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(name = "recommendation_rank", nullable = false)
    private int recommendationRank;

    @Column(name = "match_percentage", nullable = false)
    private int matchPercentage;

    @Column(name = "match_reasons", nullable = false, columnDefinition = "JSON")
    private String matchReasons;

    @Column(name = "caution_points", nullable = false, columnDefinition = "JSON")
    private String cautionPoints;

    private MateRecommendation(
            MateRecommendationBatch batch,
            Post post,
            int recommendationRank,
            int matchPercentage,
            String matchReasons,
            String cautionPoints
    ) {
        if (recommendationRank < 1 || recommendationRank > RecommendationPolicy.MAX_DAILY_RECOMMENDATIONS) {
            throw new IllegalArgumentException("Recommendation rank must be between 1 and 3");
        }
        if (matchPercentage < 0 || matchPercentage > 100) {
            throw new IllegalArgumentException("Match percentage must be between 0 and 100");
        }
        this.batch = batch;
        this.post = post;
        this.recommendationRank = recommendationRank;
        this.matchPercentage = matchPercentage;
        this.matchReasons = matchReasons;
        this.cautionPoints = cautionPoints;
    }

    public static MateRecommendation create(
            MateRecommendationBatch batch,
            Post post,
            int recommendationRank,
            int matchPercentage,
            String matchReasons,
            String cautionPoints
    ) {
        return new MateRecommendation(
                batch,
                post,
                recommendationRank,
                matchPercentage,
                matchReasons,
                cautionPoints
        );
    }
}
